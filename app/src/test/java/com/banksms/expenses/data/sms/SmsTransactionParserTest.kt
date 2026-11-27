package com.banksms.expenses.data.sms

import com.banksms.expenses.data.model.AccountType
import com.banksms.expenses.data.model.ExpenseCategory
import com.banksms.expenses.data.model.ParseTemplateEntity
import com.banksms.expenses.data.model.TransactionChannel
import com.banksms.expenses.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        assertEquals(AccountType.SAVINGS, parsed.accountType)
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
        assertEquals(ExpenseCategory.SALARY, parsed.category)
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

    @Test
    fun parsesIndianLakhFormattingCreditCardLimitAndRefNo() {
        val parsed = SmsTransactionParser.parse(
            sender = "AD-HDFCBK",
            body = "Rs.1,23,456.78 spent on HDFC Bank Card ending 8812 at Apple India on 25-11-26 UPI Ref 987654321012. Avl Lmt Rs.2,45,000.00",
            timestamp = 1_700_000_000_000L
        )

        assertNotNull(parsed)
        parsed!!
        assertEquals(123456.78, parsed.amount, 0.001)
        assertEquals("8812", parsed.accountLast4)
        assertEquals(AccountType.CREDIT, parsed.accountType)
        assertEquals(245000.0, parsed.creditLimitAfter!!, 0.001)
        assertEquals("987654321012", parsed.refNo)
        assertEquals(TransactionChannel.UPI, parsed.channel)
        assertEquals(ExpenseCategory.SHOPPING, parsed.category)
    }

    @Test
    fun detectsInternationalUsdTransaction() {
        val parsed = SmsTransactionParser.parse(
            sender = "VM-AXISBK",
            body = "USD 19.99 spent on Axis Bank Credit Card *3301 at OpenAI on 26-11-26.",
            timestamp = 1_700_000_000_000L
        )

        assertNotNull(parsed)
        parsed!!
        assertEquals("$", parsed.currency)
        assertEquals(19.99, parsed.amount, 0.001)
        assertTrue(parsed.isInternational)
        assertEquals(AccountType.CREDIT, parsed.accountType)
        assertEquals("3301", parsed.accountLast4)
    }

    @Test
    fun detectsBalanceOnlyAlertsWithoutCreatingTransaction() {
        val outcome = SmsTransactionParser.parseDetailed(
            sender = "JX-SBIINB",
            body = "Dear Customer, Available Balance in your SBI A/c XX7790 as of 26-11-26 is Rs.31,262.20",
            timestamp = 1_700_000_000_000L
        )

        assertTrue(outcome is SmsParseOutcome.BalanceOnly)
        val bal = outcome as SmsParseOutcome.BalanceOnly
        assertEquals("SBI", bal.bankName)
        assertEquals("7790", bal.accountLast4)
        assertEquals(31262.20, bal.balance, 0.001)
    }

    @Test
    fun routesUnparseableKnownBankAlertToUnparsedQueue() {
        val outcome = SmsTransactionParser.parseDetailed(
            sender = "AD-HDFCBK",
            body = "HDFC Bank Alert: Standing Instruction reference SI90812 processed for your A/c XX1234.",
            timestamp = 1_700_000_000_000L
        )

        assertTrue(outcome is SmsParseOutcome.Unparsed)
    }

    @Test
    fun respectsIgnoredSenderList() {
        val outcome = SmsTransactionParser.parseDetailed(
            sender = "AD-HDFCBK",
            body = "Amt Sent Rs.485.00 from HDFC Bank A/c XX1234 to Swiggy on 26-11-26.",
            timestamp = 1_700_000_000_000L,
            ignoredSenderCodes = setOf("HDFCBK")
        )

        assertTrue(outcome is SmsParseOutcome.Ignored)
    }

    @Test
    fun appliesCustomParseTemplateForNonStandardSms() {
        val tpl = ParseTemplateEntity(
            name = "Custom Bank Format",
            senderPattern = "MYBANK",
            regexPattern = """ALERT\|AMT=([0-9.]+)\|MERCHANT=([A-Za-z0-9 ]+)\|AC=(\d{4})""",
            amountGroup = 1,
            merchantGroup = 2,
            accountGroup = 3,
            defaultDirection = TransactionType.DEBIT
        )

        val outcome = SmsTransactionParser.parseDetailed(
            sender = "AX-MYBANK",
            body = "ALERT|AMT=799.50|MERCHANT=BlueTokai|AC=5566",
            timestamp = 1_700_000_000_000L,
            customTemplates = listOf(tpl)
        )

        assertTrue(outcome is SmsParseOutcome.Parsed)
        val txn = (outcome as SmsParseOutcome.Parsed).transaction
        assertEquals(799.50, txn.amount, 0.001)
        assertEquals("BlueTokai", txn.merchant)
        assertEquals("5566", txn.accountLast4)
        assertFalse(txn.isInternational)
    }

    @Test
    fun parsesAtmAndNachAndNeftChannels() {
        val atm = SmsTransactionParser.parse(
            sender = "JX-SBIINB",
            body = "Rs.3,000.00 withdrawn at SBI ATM Indiranagar from A/c X7790 on 22-11-26.",
            timestamp = 1_700_000_000_000L
        )!!
        val nach = SmsTransactionParser.parse(
            sender = "AD-HDFCBK",
            body = "Rs.5,000.00 debited from HDFC Bank A/c XX1234 towards ACH D- Mutual Fund SIP Folio 99120 on 15-11-26.",
            timestamp = 1_700_000_000_000L
        )!!
        val neft = SmsTransactionParser.parse(
            sender = "VK-ICICIB",
            body = "ICICI Bank Acct XX4521 credited with Rs 15,000.00 via NEFT from Acme Corp Ref UTR12345678.",
            timestamp = 1_700_000_000_000L
        )!!

        assertEquals(TransactionChannel.ATM, atm.channel)
        assertEquals(ExpenseCategory.ATM, atm.category)
        assertEquals(TransactionChannel.NACH_ECS, nach.channel)
        assertEquals(ExpenseCategory.INVESTMENTS, nach.category)
        assertEquals(TransactionChannel.NEFT_IMPS, neft.channel)
    }
}
