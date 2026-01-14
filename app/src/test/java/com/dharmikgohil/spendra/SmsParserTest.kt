package com.dharmikgohil.spendra

import org.junit.Assert.*
import org.junit.Test

class SmsParserTest {

    private val parser = SmsParser()

    @Test
    fun testHdfcDebit() {
        val message = "Rs.123.00 debited from a/c **1234 to ZOMATO LIMITED on 12-01-25. Avl Bal: Rs. 5000.00"
        val transaction = parser.parseMessage(message)
        assertNotNull(transaction)
        assertEquals(123.0, transaction!!.amount, 0.01)
        assertEquals("DEBIT", transaction.type)
        assertEquals("Zomato Limited", transaction.merchant) // Expecting clean name
        assertEquals("Food", transaction.category)
        assertEquals(5000.0, transaction.balance!!, 0.01)
    }

    @Test
    fun testSbiUpiSent() {
        val message = "Sent Rs. 450.00 to AMAZON PAY INDIA via UPI. Ref: 123456789. Bal: Rs. 1000.00"
        val transaction = parser.parseMessage(message)
        assertNotNull(transaction)
        assertEquals(450.0, transaction!!.amount, 0.01)
        assertEquals("DEBIT", transaction.type)
        assertEquals("Amazon Pay India", transaction.merchant)
        assertEquals("Shopping", transaction.category)
        assertEquals(1000.0, transaction.balance!!, 0.01)
    }

    @Test
    fun testIciciSpent() {
        val message = "Spent Rs 200.00 on Credit Card XX1234 at UBER INDIA SYSTEMS. Avl Lmt: Rs 50000"
        val transaction = parser.parseMessage(message)
        assertNotNull(transaction)
        assertEquals(200.0, transaction!!.amount, 0.01)
        assertEquals("DEBIT", transaction.type)
        assertEquals("Uber India Systems", transaction.merchant)
        assertEquals("Travel", transaction.category)
    }

    @Test
    fun testCredit() {
        val message = "Acct XX123 credited with Rs. 5000.00 on 01-Jan-25. Info: SALARY. Avl Bal: Rs. 50000.00"
        val transaction = parser.parseMessage(message)
        assertNotNull(transaction)
        assertEquals(5000.0, transaction!!.amount, 0.01)
        assertEquals("CREDIT", transaction.type)
        assertEquals(50000.0, transaction.balance!!, 0.01)
    }
    
    @Test
    fun testAtmWithdrawal() {
        val message = "Rs. 500.00 withdrawn from ATM 1234 on 12-01-25. Avl Bal: Rs. 1000.00"
        val transaction = parser.parseMessage(message)
        assertNotNull(transaction)
        assertEquals(500.0, transaction!!.amount, 0.01)
        assertEquals("DEBIT", transaction.type)
        assertEquals("Cash Withdrawal", transaction.merchant)
    }
}
