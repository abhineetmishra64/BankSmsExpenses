package com.banksms.expenses.data.sms

import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsTransactionParserTest {

    @Test
    fun parsesHdfcDebitWithBalanceAndMerchant() {
        val parsed = SmsTransactionParser.parse(
            sender = "AD-HDFCBK",
            body = "Amt Sent Rs.485.00 from HDFC Bank A/c XX1234 to Swiggy on 26-11-26. Avl Bal Rs.42,310.50",
            timestamp = 1_700_000_000_000L
        )

        assertNotNull(parsed)
        parsed!!
        assertEquals("HDFC Bank", parsed.bankName)
        assertEquals(TransactionType.DEBIT, parsed.type)
        assertEquals(485.0, parsed.amount, 0.001)
        assertEquals("₹", parsed.currency)
        assertEquals("1234", parsed.accountLast4)
        assertEquals("Swiggy", parsed.merchant)
        assertEquals(ExpenseCategory.FOOD, parsed.category)
        assertEquals(42310.50, parsed.balanceAfter!!, 0.001)
    }

    @Test
    fun parsesIciciDebitEvenWhenBeneficiaryCreditedAppearsLater() {
        val parsed = SmsTransactionParser.parse(
            sender = "VK-ICICIB",
            body = "ICICI Bank Acct XX4521 debited for Rs 320.00 on 25-11-26; Uber India credited. Avl Bal Rs 18,900.00",
            timestamp = 1_700_000_000_000L
        )

        assertNotNull(parsed)
        parsed!!
        assertEquals("ICICI Bank", parsed.bankName)
        assertEquals(TransactionType.DEBIT, parsed.type)
        assertEquals(320.0, parsed.amount, 0.001)
        assertEquals("4521", parsed.accountLast4)
        assertEquals("Uber India", parsed.merchant)
        assertEquals(ExpenseCategory.TRANSPORT, parsed.category)
    }

    @Test
    fun parsesSalaryCredit() {
        val parsed = SmsTransactionParser.parse(
            sender = "VK-ICICIB",
            body = "Dear Customer, your ICICI Bank Acct XX4521 is credited with Rs 75,000.00 on 20-11-26 towards Salary.",
            timestamp = 1_700_000_000_000L
        )

        assertNotNull(parsed)
        parsed!!
        assertEquals("ICICI Bank", parsed.bankName)
        assertEquals(TransactionType.CREDIT, parsed.type)
        assertEquals(75000.0, parsed.amount, 0.001)
        assertEquals("Salary", parsed.merchant)
    }

    @Test
    fun ignoresOtpAndFailedTransactions() {
        val otp = SmsTransactionParser.parse(
            sender = "AD-HDFCBK",
            body = "OTP is 491822 for transaction of Rs.1,200.00 at Flipkart. Do not share with anyone.",
            timestamp = 1_700_000_000_000L
        )
        val failed = SmsTransactionParser.parse(
            sender = "VM-AXISBK",
            body = "Transaction of INR 999.00 on Axis Bank Card XX3301 at Amazon was declined due to insufficient balance.",
            timestamp = 1_700_000_000_000L
        )

        assertNull(otp)
        assertNull(failed)
    }

    @Test
    fun resolvesUnknownBankToSeparateBucket() {
        val parsed = SmsTransactionParser.parse(
            sender = "JD-NORTBNK",
            body = "Rs. 600.00 debited from Northstar Bank A/c XX9911 at Cafe Coffee Day on 26-11-26.",
            timestamp = 1_700_000_000_000L
        )

        assertNotNull(parsed)
        parsed!!
        assertEquals("Northstar Bank", parsed.bankName)
        assertEquals(ExpenseCategory.FOOD, parsed.category)
    }

    @Test
    fun producesSameFingerprintForDuplicateAndDifferentForDistinct() {
        val body = "INR 649.00 spent on Axis Bank Card no. XX3301 at Netflix on 23-11-26."
        val first = SmsTransactionParser.parse(sender = "VM-AXISBK", body = body, timestamp = 1_700_000_000_000L)!!
        val duplicate = SmsTransactionParser.parse(sender = "VM-AXISBK", body = body, timestamp = 1_700_000_010_000L)!!
        val different = SmsTransactionParser.parse(sender = "VM-AXISBK", body = body, timestamp = 1_700_000_300_000L)!!

        assertEquals(first.fingerprint, duplicate.fingerprint)
        assertNotEquals(first.fingerprint, different.fingerprint)
    }
}
