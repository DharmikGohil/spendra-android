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
    val rawTextHash: String,
    val category: String? = null
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
    val balance: Double? = null,
    val category: CategoryDto? = null
)

data class SyncResponse(
    val success: Boolean,
    val created: Int,
    val skipped: Int,
    val errors: List<String>,
    val data: List<TransactionDto>? = null
)

/**
 * Simple SMS parser for Indian bank/UPI messages
 */
class SmsParser {

    fun parseMessage(message: String): Transaction? {
        val cleanMessage = message.replace(Regex("\\s+"), " ").trim()
        
        // 1. Sent to / Paid to (UPI)
        // "Sent Rs. 450.00 to AMAZON PAY INDIA via UPI"
        // "Paid Rs. 120.00 to ZOMATO"
        val sentPattern = Regex("""(?i)(?:sent|paid).*?rs\.?\s*(\d+(?:\.\d{2})?).*?to\s+(.+?)(?:\s+via|\s+on|\s+ref|$)""")
        sentPattern.find(cleanMessage)?.let { match ->
            return createTransaction(match, "DEBIT", cleanMessage, "UPI")
        }

        // 2. Debited from (Bank)
        // "Rs.123.00 debited from a/c ... to ZOMATO LIMITED on ..."
        val debitPattern = Regex("""(?i)rs\.?\s*(\d+(?:\.\d{2})?).*?debited.*?to\s+(.+?)(?:\s+on|\s+ref|$)""")
        debitPattern.find(cleanMessage)?.let { match ->
            return createTransaction(match, "DEBIT", cleanMessage, "BANK")
        }

        // 3. Spent on (Card)
        // "Spent Rs 200.00 on Credit Card ... at UBER INDIA SYSTEMS"
        val spentPattern = Regex("""(?i)spent.*?rs\.?\s*(\d+(?:\.\d{2})?).*?at\s+(.+?)(?:\s+on|\.|$)""")
        spentPattern.find(cleanMessage)?.let { match ->
            return createTransaction(match, "DEBIT", cleanMessage, "CARD")
        }

        // 4. Withdrawn (ATM)
        // "Rs. 500.00 withdrawn from ATM"
        val withdrawnPattern = Regex("""(?i)rs\.?\s*(\d+(?:\.\d{2})?).*?withdrawn.*?(?:from|at)\s+(.+?)(?:\s+on|\.|$)""")
        withdrawnPattern.find(cleanMessage)?.let { match ->
            return createTransaction(match, "DEBIT", cleanMessage, "ATM", isWithdrawal = true)
        }
        
        // 5. Credited
        // "Acct ... credited with Rs. 5000.00"
        val creditPattern = Regex("""(?i)credited.*?rs\.?\s*(\d+(?:\.\d{2})?)""")
        creditPattern.find(cleanMessage)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return null
            // Merchant/Source is harder to find in credit messages, often "SALARY" or "UPI"
            // For now, we'll try to find "from ..." or default to "Unknown Source"
            val fromPattern = Regex("""(?i)from\s+(.+?)(?:\s+on|\.|$)""")
            val merchant = fromPattern.find(cleanMessage)?.groupValues?.get(1) ?: "Deposit/Transfer"
            
            return Transaction(
                amount = amount,
                type = "CREDIT",
                merchant = cleanMerchantName(merchant),
                source = "BANK",
                timestamp = System.currentTimeMillis(),
                balance = extractBalance(cleanMessage),
                rawTextHash = generateHash(cleanMessage),
                category = "Income"
            )
        }

        return null
    }

    private fun createTransaction(
        match: MatchResult, 
        type: String, 
        message: String, 
        source: String,
        isWithdrawal: Boolean = false
    ): Transaction? {
        val amount = match.groupValues[1].toDoubleOrNull() ?: return null
        var merchant = match.groupValues[2]
        
        if (isWithdrawal) {
            merchant = "Cash Withdrawal"
        }

        val cleanMerchant = cleanMerchantName(merchant)
        val category = categorizeMerchant(cleanMerchant, isWithdrawal)

        return Transaction(
            amount = amount,
            type = type,
            merchant = cleanMerchant,
            source = source,
            timestamp = System.currentTimeMillis(),
            balance = extractBalance(message),
            rawTextHash = generateHash(message),
            category = category
        )
    }

    private fun cleanMerchantName(rawName: String): String {
        var name = rawName
        
        // Remove common prefixes/suffixes
        val removeList = listOf("UPI/", "VPS/", "IPS/", "POS/", "ECOM", "MUMBAI", "BANGALORE", "DELHI", "HYDERABAD", "CHENNAI", "KOLKATA", "PUNE")
        for (item in removeList) {
            name = name.replace(item, "", ignoreCase = true)
        }
        
        // Remove dates like 12-01-25
        name = name.replace(Regex("""\d{2}-\d{2}-\d{2,4}"""), "")
        
        // Remove special chars and extra spaces
        name = name.replace(Regex("""[^\w\s]"""), " ").trim()
        name = name.replace(Regex("""\s+"""), " ")

        // Capitalize words
        return name.split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
    }

    private fun categorizeMerchant(merchant: String, isWithdrawal: Boolean): String {
        if (isWithdrawal) return "Cash"
        
        val lowerMerchant = merchant.lowercase()
        
        return when {
            lowerMerchant.containsAny("zomato", "swiggy", "mcdonalds", "pizza", "burger", "briyani", "restaurant", "cafe", "coffee", "starbucks", "kfc", "dominos", "food") -> "Food"
            lowerMerchant.containsAny("uber", "ola", "rapido", "metro", "irctc", "airline", "flight", "bus", "train", "fuel", "petrol", "shell", "hpcl", "bpcl", "indian oil") -> "Travel"
            lowerMerchant.containsAny("amazon", "flipkart", "myntra", "ajio", "retail", "mart", "store", "shop", "mall", "decathlon", "nike", "adidas", "zara", "h&m") -> "Shopping"
            lowerMerchant.containsAny("jio", "airtel", "vodafone", "vi", "bsnl", "act", "hathway", "electricity", "bescom", "bill", "recharge", "tatasky", "dth") -> "Bills"
            lowerMerchant.containsAny("netflix", "spotify", "prime", "hotstar", "cinema", "movie", "pvr", "inox", "bookmyshow", "youtube", "apple") -> "Entertainment"
            lowerMerchant.containsAny("pharmacy", "medical", "hospital", "clinic", "doctor", "apollo", "1mg", "pharmeasy") -> "Health"
            lowerMerchant.containsAny("salary", "interest", "refund", "cashback") -> "Income"
            else -> "General"
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
    
    private fun extractBalance(message: String): Double? {
        val balancePattern = Regex("""(?i)(?:avl|bal|balance).*?rs\.?\s*(\d+(?:\.\d{2})?)""")
        return balancePattern.find(message)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun generateHash(message: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(message.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(32)
    }
}
