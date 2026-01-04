package com.dharmikgohil.spendra

import com.google.gson.annotations.SerializedName
import java.security.MessageDigest

// Data classes
data class Transaction(
    val amount: Double,
    val type: String,
    val merchant: String,
    val source: String,
    val timestamp: Long,
    val balance: Double? = null,
    val rawTextHash: String
)

data class SyncRequest(
    val deviceId: String,
    val transactions: List<TransactionDto>
)

data class TransactionDto(
    val amount: Double,
    val type: String,
    val merchant: String,
    val source: String,
    val timestamp: String,
    @SerializedName("rawTextHash") val rawTextHash: String? = null,
    val balance: Double? = null
)

data class SyncResponse(
    val success: Boolean,
    val created: Int,
    val skipped: Int,
    val errors: List<String>
)

/**
 * Simple SMS parser for Indian bank/UPI messages
 */
class SmsParser {
    
    fun parseMessage(message: String): Transaction? {
        if (!message.contains("rs", ignoreCase = true)) return null
        
        // Debit pattern 1: "debited Rs 500"
        val debitPattern1 = Regex("""(?i)debited.*?rs\.?\s*(\d+(?:\.\d{2})?).*?(?:to|from)\s*(.+?)(?:\s+on|$)""")
        debitPattern1.find(message)?.let { match ->
            return createTransaction(match, "DEBIT", message)
        }

        // Debit pattern 2: "Rs 500 debited"
        val debitPattern2 = Regex("""(?i)rs\.?\s*(\d+(?:\.\d{2})?)\s+debited.*?(?:to|from)\s*(.+?)(?:\s+on|$)""")
        debitPattern2.find(message)?.let { match ->
            return createTransaction(match, "DEBIT", message)
        }
        
        // Credit pattern
        val creditPattern = Regex("""(?i)credited.*?rs\.?\s*(\d+(?:\.\d{2})?).*?from\s*(.+?)(?:\s+to|$)""")
        creditPattern.find(message)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            val merchant = match.groupValues[2].trim().take(50)
            val balance = extractBalance(message)
            
            return Transaction(
                amount = amount,
                type = "CREDIT",
                merchant = merchant,
                source = "UPI",
                timestamp = System.currentTimeMillis(),
                balance = balance,
                rawTextHash = generateHash(message)
            )
        }
        
        return null
    }
    
    private fun extractBalance(message: String): Double? {
        val balancePattern = Regex("""(?i)(?:avl|bal|balance).*?rs\.?\s*(\d+(?:\.\d{2})?)""")
        return balancePattern.find(message)?.groupValues?.get(1)?.toDoubleOrNull()
    }
    
    private fun createTransaction(match: MatchResult, type: String, message: String): Transaction? {
        val amount = match.groupValues[1].toDoubleOrNull() ?: return null
        val merchant = match.groupValues[2].trim().take(50)
        val balance = extractBalance(message)
        
        return Transaction(
            amount = amount,
            type = type,
            merchant = merchant,
            source = "UPI",
            timestamp = System.currentTimeMillis(),
            balance = balance,
            rawTextHash = generateHash(message)
        )
    }

    private fun generateHash(message: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(message.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(32)
    }
}
