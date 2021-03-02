package uk.nhs.nhsx.keyfederation.upload

import com.amazonaws.services.lambda.runtime.Context
import uk.nhs.nhsx.core.Environment
import uk.nhs.nhsx.core.EnvironmentKeys
import uk.nhs.nhsx.core.ObjectKeyFilters
import uk.nhs.nhsx.core.StandardSigning
import uk.nhs.nhsx.core.SystemClock
import uk.nhs.nhsx.core.aws.s3.AwsS3
import uk.nhs.nhsx.core.aws.s3.AwsS3Client
import uk.nhs.nhsx.core.aws.s3.BucketName
import uk.nhs.nhsx.core.aws.secretsmanager.AwsSecretManager
import uk.nhs.nhsx.core.aws.secretsmanager.SecretManager
import uk.nhs.nhsx.core.aws.ssm.AwsSsmParameters
import uk.nhs.nhsx.core.events.Event
import uk.nhs.nhsx.core.events.EventCategory
import uk.nhs.nhsx.core.events.Events
import uk.nhs.nhsx.core.events.InfoEvent
import uk.nhs.nhsx.core.events.PrintingJsonEvents
import uk.nhs.nhsx.core.scheduled.Scheduling
import uk.nhs.nhsx.core.scheduled.SchedulingHandler
import uk.nhs.nhsx.diagnosiskeydist.s3.SubmissionFromS3Repository
import uk.nhs.nhsx.keyfederation.BatchTagDynamoDBService
import uk.nhs.nhsx.keyfederation.BatchTagService
import uk.nhs.nhsx.keyfederation.InteropClient
import java.time.Instant
import java.util.function.Supplier

/**
 * Key Federation upload lambda
 *
 *
 * doc/architecture/api-contracts/diagnosis-key-federation.md
 */
class KeyFederationUploadHandler @JvmOverloads constructor(
    private val environment: Environment = Environment.fromSystem(),
    private val clock: Supplier<Instant> = SystemClock.CLOCK,
    events: Events = PrintingJsonEvents(clock),
    private val submissionBucket: BucketName = environment.access.required(EnvironmentKeys.SUBMISSION_BUCKET_NAME),
    private val config: KeyFederationUploadConfig = KeyFederationUploadConfig.fromEnvironment(environment),
    secretManager: SecretManager = AwsSecretManager(),
    private val batchTagService: BatchTagService = BatchTagDynamoDBService(config.stateTableName),
    private val interopClient: InteropClient = buildInteropClient(config, secretManager, events),
    private val awsS3Client: AwsS3 = AwsS3Client(events)
) : SchedulingHandler(events) {

    override fun handler() = Scheduling.Handler { _, context ->
        InteropConnectorUploadStats(loadKeysAndUploadToFederatedServer(context))
    }

    private fun loadKeysAndUploadToFederatedServer(context: Context) = if (config.uploadFeatureFlag.isEnabled) {
        try {
            val objectKeyFilter = ObjectKeyFilters.federated().withPrefixes(config.federatedKeyUploadPrefixes)
            val submissionRepository =
                SubmissionFromS3Repository(awsS3Client, objectKeyFilter, submissionBucket, events)

            DiagnosisKeysUploadService(
                clock,
                interopClient,
                submissionRepository,
                batchTagService,
                config.region,
                config.uploadRiskLevelDefaultEnabled,
                config.uploadRiskLevelDefault,
                config.initialUploadHistoryDays,
                config.maxUploadBatchSize,
                config.maxSubsequentBatchUploadCount,
                context,
                events
            ).loadKeysAndUploadToFederatedServer()
        } catch (e: Exception) {
            throw RuntimeException("Upload keys failed with error", e)
        }
    } else {
        events(javaClass, InfoEvent("Upload to interop has been disabled, skipping this step"))
        0
    }
}

data class InteropConnectorUploadStats(val processedSubmissions: Int) : Event(EventCategory.Info)

private fun buildInteropClient(
    config: KeyFederationUploadConfig,
    secretManager: SecretManager,
    events: Events
): InteropClient {
    val authTokenSecretValue = secretManager.getSecret(config.interopAuthTokenSecretName)
        .orElseThrow { RuntimeException("Unable to retrieve authorization token from secrets storage") }

    val signer = StandardSigning.signContentWithKeyFromParameter(AwsSsmParameters(), config.signingKeyParameterName)

    return InteropClient(
        config.interopBaseUrl,
        authTokenSecretValue.value,
        JWS(signer),
        events
    )
}
