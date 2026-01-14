package com.dharmikgohil.spendra

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.dharmikgohil.spendra.data.local.TransactionEntity

class SmsReceiver : BroadcastReceiver() {

    private val smsParser = SmsParser()

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "SmsReceiver triggered! Action: ${intent.action}")
        
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d(TAG, "Wrong action, ignoring")
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        Log.d(TAG, "Got ${messages.size} SMS messages")
        
        CoroutineScope(Dispatchers.IO).launch {
            for (smsMessage in messages) {
                val messageBody = smsMessage.messageBody ?: continue
                val sender = smsMessage.displayOriginatingAddress ?: continue

                Log.d(TAG, "SMS from: $sender, length: ${sender.length}")
                Log.d(TAG, "Message: $messageBody")

                // Only process messages from banks (6-digit sender IDs)
                // Relaxed check for emulator/testing where sender might be "01"
                if (!isBankMessage(sender)) {
                    Log.w(TAG, "Sender $sender is NOT a standard bank sender (length=${sender.length}), but attempting to parse anyway.")
                }

                Log.d(TAG, "Processing SMS from $sender")
                
                val transaction = smsParser.parseMessage(messageBody)
                if (transaction != null) {
                    Log.d(TAG, "Parsed: ${transaction.merchant} - ₹${transaction.amount}")
                    
                    val dao = (context.applicationContext as SpendraApplication).database.transactionDao()

                    // Save to Local DB
                    try {
                        val entity = transaction.toEntity()
                        dao.insertTransaction(entity)
                        Log.d(TAG, "Saved to local DB")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to save to local DB", e)
                    }
                    
                    // Sync to backend
                    try {
                        val deviceId = Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                        
                        Log.d(TAG, "Syncing to backend for device: $deviceId")
                        
                        val response = ApiClient.api.syncTransactions(
                            SyncRequest(
                                deviceId = deviceId,
                                transactions = listOf(transaction.toDto())
                            )
                        )
                        
                        Log.d(TAG, "Synced: ${response.created} created, ${response.skipped} skipped")
                        
                        // Update local categories from backend response
                        response.data?.forEach { dto ->
                            if (dto.category != null && dto.rawTextHash != null) {
                                dao.updateCategory(dto.rawTextHash, dto.category.name)
                                Log.d(TAG, "Updated category for ${dto.merchant}: ${dto.category.name}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Sync failed", e)
                    }
                } else {
                    Log.d(TAG, "Failed to parse transaction from message")
                }
            }
        }
    }

    private fun isBankMessage(sender: String): Boolean {
        return sender.length == 6 && sender.any { it.isLetter() }
    }

    private fun Transaction.toEntity() = TransactionEntity(
        amount = amount,
        type = type,
        merchant = merchant,
        source = source,
        timestamp = timestamp,
        balance = balance,
        rawTextHash = rawTextHash,
        category = category,
        isSynced = false
    )

    private fun Transaction.toDto() = TransactionDto(
        amount = amount,
        type = type,
        merchant = merchant,
        source = source,
        timestamp = formatTimestamp(timestamp),
        rawTextHash = rawTextHash,
        balance = balance,
        category = null // Backend ignores this, and we can't construct CategoryDto from string
    )

    private fun formatTimestamp(millis: Long): String {
        return Instant.ofEpochMilli(millis).toString()
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
