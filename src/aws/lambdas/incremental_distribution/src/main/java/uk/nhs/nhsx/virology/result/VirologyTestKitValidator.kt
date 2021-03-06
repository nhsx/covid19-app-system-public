package uk.nhs.nhsx.virology.result

import uk.nhs.nhsx.core.exceptions.ApiResponseException
import uk.nhs.nhsx.core.exceptions.HttpStatusCode.UNPROCESSABLE_ENTITY_422
import uk.nhs.nhsx.virology.TestKit
import uk.nhs.nhsx.virology.TestKit.LAB_RESULT
import uk.nhs.nhsx.virology.TestKit.RAPID_RESULT
import uk.nhs.nhsx.virology.TestKit.RAPID_SELF_REPORTED
import uk.nhs.nhsx.virology.result.TestResult.Positive

object VirologyTestKitValidator {
    fun validate(testKit: TestKit, testResult: TestResult) {
        val pcr = LAB_RESULT === testKit
        val lfd = RAPID_RESULT === testKit || RAPID_SELF_REPORTED === testKit
        val lfdPositive = lfd && Positive == testResult
        val isValid = pcr || lfdPositive
        if (!isValid) {
            throw ApiResponseException(UNPROCESSABLE_ENTITY_422, "validation error: Invalid test type value")
        }
    }
}
