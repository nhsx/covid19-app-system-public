package uk.nhs.nhsx.isolationpayment

import uk.nhs.nhsx.virology.IpcTokenId
import java.security.SecureRandom

object IpcTokenIdGenerator {
    /**
     * Generates unique ID from secure random source of 32 bytes with hex representation
     */
    @JvmStatic
    fun getToken(): IpcTokenId {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return IpcTokenId.of(convertBytesToHex(bytes))
    }

    private fun convertBytesToHex(bytes: ByteArray): String {
        val result = StringBuilder()
        for (byteValue in bytes) {
            result.append(String.format("%02x", byteValue))
        }
        return result.toString()
    }
}
