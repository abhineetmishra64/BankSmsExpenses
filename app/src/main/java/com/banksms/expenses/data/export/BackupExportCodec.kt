package com.banksms.expenses.data.export

import com.banksms.expenses.data.model.CategoryRuleEntity
import com.banksms.expenses.data.model.CategorySource
import com.banksms.expenses.data.model.ExclusionReason
import com.banksms.expenses.data.model.ExclusionRuleEntity
import com.banksms.expenses.data.model.ExclusionSource
import com.banksms.expenses.data.model.MerchantAliasEntity
import com.banksms.expenses.data.model.ParseTemplateEntity
import com.banksms.expenses.data.model.TransactionChannel
import com.banksms.expenses.data.model.TransactionEntity
import com.banksms.expenses.data.model.TransactionType
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class RulesBundle(
    val categoryRules: List<CategoryRuleEntity>,
    val exclusionRules: List<ExclusionRuleEntity>,
    val aliases: List<MerchantAliasEntity>,
    val templates: List<ParseTemplateEntity>
)

object CsvExporter {
    fun exportTransactions(
        transactions: List<TransactionEntity>,
        categoryNamesById: Map<Long, String>
    ): String {
        val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.appendLine("Date,Type,Merchant,Category,Bank,AccountLast4,Amount,Currency,Channel,RefNo,Excluded,ExclusionReason,Note,Tags")
        for (txn in transactions) {
            val cols = listOf(
                dateFmt.format(Date(txn.txnTime)),
                txn.type.name,
                txn.merchant,
                categoryNamesById[txn.categoryId] ?: "Other",
                txn.bankName,
                txn.accountLast4.orEmpty(),
                "%.2f".format(Locale.US, txn.amount),
                txn.currency,
                txn.channel.name,
                txn.refNo.orEmpty(),
                txn.isExcluded.toString(),
                txn.exclusionReason?.name.orEmpty(),
                txn.note,
                txn.tags
            )
            sb.appendLine(cols.joinToString(",") { escapeCsv(it) })
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        return if (!needsQuotes) {
            value
        } else {
            "\"${value.replace("\"", "\"\"")}\""
        }
    }
}

object BackupCrypto {
    private const val MAGIC = "BKSMSEXP1:"
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val ITERATIONS = 65_536

    fun encrypt(plainText: String, password: String): String {
        require(password.isNotEmpty()) { "Password must not be empty" }
        val rng = SecureRandom()
        val salt = ByteArray(SALT_LEN).also { rng.nextBytes(it) }
        val iv = ByteArray(IV_LEN).also { rng.nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combined = ByteArray(salt.size + iv.size + encrypted.size)
        System.arraycopy(salt, 0, combined, 0, salt.size)
        System.arraycopy(iv, 0, combined, salt.size, iv.size)
        System.arraycopy(encrypted, 0, combined, salt.size + iv.size, encrypted.size)
        return MAGIC + Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(payload: String, password: String): String {
        require(payload.startsWith(MAGIC)) { "Unsupported backup file format" }
        require(password.isNotEmpty()) { "Password must not be empty" }
        val bytes = Base64.getDecoder().decode(payload.removePrefix(MAGIC).trim())
        require(bytes.size > SALT_LEN + IV_LEN) { "Corrupted backup data" }
        val salt = bytes.copyOfRange(0, SALT_LEN)
        val iv = bytes.copyOfRange(SALT_LEN, SALT_LEN + IV_LEN)
        val cipherText = bytes.copyOfRange(SALT_LEN + IV_LEN, bytes.size)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}

/**
 * Pure-Kotlin JSON & line-delimited codec for Rules and Backup export/import without org.json dependency,
 * allowing reliable execution in both JVM unit tests and Android runtime.
 */
object RulesAndBackupCodec {

    fun exportRulesJson(bundle: RulesBundle): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"version\": 1,\n")
        sb.append("  \"categoryRules\": [\n")
        bundle.categoryRules.forEachIndexed { idx, r ->
            sb.append("    {")
            sb.append("\"name\":${q(r.name)},")
            sb.append("\"priority\":${r.priority},")
            sb.append("\"categoryId\":${r.categoryId},")
            sb.append("\"merchantPattern\":${qn(r.merchantPattern)},")
            sb.append("\"merchantMatchMode\":${q(r.merchantMatchMode)},")
            sb.append("\"bodyKeyword\":${qn(r.bodyKeyword)},")
            sb.append("\"senderPattern\":${qn(r.senderPattern)},")
            sb.append("\"vpaPattern\":${qn(r.vpaPattern)},")
            sb.append("\"minAmount\":${r.minAmount ?: "null"},")
            sb.append("\"maxAmount\":${r.maxAmount ?: "null"},")
            sb.append("\"direction\":${qn(r.direction?.name)},")
            sb.append("\"regexPattern\":${qn(r.regexPattern)},")
            sb.append("\"alsoExclude\":${r.alsoExclude},")
            sb.append("\"exclusionReason\":${qn(r.exclusionReason?.name)},")
            sb.append("\"enabled\":${r.enabled}")
            sb.append("}")
            if (idx < bundle.categoryRules.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")
        sb.append("  \"exclusionRules\": [\n")
        bundle.exclusionRules.forEachIndexed { idx, r ->
            sb.append("    {")
            sb.append("\"name\":${q(r.name)},")
            sb.append("\"merchantPattern\":${qn(r.merchantPattern)},")
            sb.append("\"bodyKeyword\":${qn(r.bodyKeyword)},")
            sb.append("\"regexPattern\":${qn(r.regexPattern)},")
            sb.append("\"exactAmount\":${r.exactAmount ?: "null"},")
            sb.append("\"reason\":${q(r.reason.name)},")
            sb.append("\"enabled\":${r.enabled}")
            sb.append("}")
            if (idx < bundle.exclusionRules.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")
        sb.append("  \"aliases\": [\n")
        bundle.aliases.forEachIndexed { idx, a ->
            sb.append("    {")
            sb.append("\"rawPattern\":${q(a.rawPattern)},")
            sb.append("\"canonicalName\":${q(a.canonicalName)},")
            sb.append("\"defaultCategoryId\":${a.defaultCategoryId ?: "null"}")
            sb.append("}")
            if (idx < bundle.aliases.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")
        sb.append("  \"templates\": [\n")
        bundle.templates.forEachIndexed { idx, t ->
            sb.append("    {")
            sb.append("\"name\":${q(t.name)},")
            sb.append("\"senderPattern\":${q(t.senderPattern)},")
            sb.append("\"regexPattern\":${q(t.regexPattern)},")
            sb.append("\"amountGroup\":${t.amountGroup},")
            sb.append("\"merchantGroup\":${t.merchantGroup ?: "null"},")
            sb.append("\"accountGroup\":${t.accountGroup ?: "null"},")
            sb.append("\"balanceGroup\":${t.balanceGroup ?: "null"},")
            sb.append("\"defaultDirection\":${qn(t.defaultDirection?.name)},")
            sb.append("\"enabled\":${t.enabled}")
            sb.append("}")
            if (idx < bundle.templates.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        return sb.toString()
    }

    fun importRulesJson(json: String): RulesBundle {
        val root = JsonMiniParser(json).parseObject()
        val catRules = (root["categoryRules"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            CategoryRuleEntity(
                name = m["name"] as? String ?: "Imported Rule",
                priority = (m["priority"] as? Number)?.toInt() ?: 100,
                categoryId = (m["categoryId"] as? Number)?.toLong() ?: 22L,
                merchantPattern = m["merchantPattern"] as? String,
                merchantMatchMode = m["merchantMatchMode"] as? String ?: "CONTAINS",
                bodyKeyword = m["bodyKeyword"] as? String,
                senderPattern = m["senderPattern"] as? String,
                vpaPattern = m["vpaPattern"] as? String,
                minAmount = (m["minAmount"] as? Number)?.toDouble(),
                maxAmount = (m["maxAmount"] as? Number)?.toDouble(),
                direction = (m["direction"] as? String)?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() },
                regexPattern = m["regexPattern"] as? String,
                alsoExclude = m["alsoExclude"] as? Boolean ?: false,
                exclusionReason = (m["exclusionReason"] as? String)?.let { runCatching { ExclusionReason.valueOf(it) }.getOrNull() },
                enabled = m["enabled"] as? Boolean ?: true
            )
        } ?: emptyList()

        val exclRules = (root["exclusionRules"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val reasonStr = m["reason"] as? String ?: ExclusionReason.OTHER.name
            ExclusionRuleEntity(
                name = m["name"] as? String ?: "Imported Exclusion",
                merchantPattern = m["merchantPattern"] as? String,
                bodyKeyword = m["bodyKeyword"] as? String,
                regexPattern = m["regexPattern"] as? String,
                exactAmount = (m["exactAmount"] as? Number)?.toDouble(),
                reason = runCatching { ExclusionReason.valueOf(reasonStr) }.getOrDefault(ExclusionReason.OTHER),
                enabled = m["enabled"] as? Boolean ?: true
            )
        } ?: emptyList()

        val aliases = (root["aliases"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val rawPat = m["rawPattern"] as? String ?: return@mapNotNull null
            val canon = m["canonicalName"] as? String ?: return@mapNotNull null
            MerchantAliasEntity(
                rawPattern = rawPat,
                canonicalName = canon,
                defaultCategoryId = (m["defaultCategoryId"] as? Number)?.toLong()
            )
        } ?: emptyList()

        val templates = (root["templates"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val regex = m["regexPattern"] as? String ?: return@mapNotNull null
            ParseTemplateEntity(
                name = m["name"] as? String ?: "Imported Template",
                senderPattern = m["senderPattern"] as? String ?: "",
                regexPattern = regex,
                amountGroup = (m["amountGroup"] as? Number)?.toInt() ?: 1,
                merchantGroup = (m["merchantGroup"] as? Number)?.toInt(),
                accountGroup = (m["accountGroup"] as? Number)?.toInt(),
                balanceGroup = (m["balanceGroup"] as? Number)?.toInt(),
                defaultDirection = (m["defaultDirection"] as? String)?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() },
                enabled = m["enabled"] as? Boolean ?: true
            )
        } ?: emptyList()

        return RulesBundle(catRules, exclRules, aliases, templates)
    }

    fun exportBackupJson(
        transactions: List<TransactionEntity>,
        rulesBundle: RulesBundle
    ): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"version\": 1,\n")
        sb.append("  \"rules\": ").append(exportRulesJson(rulesBundle)).append(",\n")
        sb.append("  \"transactions\": [\n")
        transactions.forEachIndexed { idx, t ->
            sb.append("    {")
            sb.append("\"sender\":${q(t.sender)},")
            sb.append("\"bankName\":${q(t.bankName)},")
            sb.append("\"accountLast4\":${qn(t.accountLast4)},")
            sb.append("\"amount\":${t.amount},")
            sb.append("\"currency\":${q(t.currency)},")
            sb.append("\"type\":${q(t.type.name)},")
            sb.append("\"merchant\":${q(t.merchant)},")
            sb.append("\"rawMerchant\":${q(t.rawMerchant)},")
            sb.append("\"categoryId\":${t.categoryId},")
            sb.append("\"categorySource\":${q(t.categorySource.name)},")
            sb.append("\"channel\":${q(t.channel.name)},")
            sb.append("\"refNo\":${qn(t.refNo)},")
            sb.append("\"balanceAfter\":${t.balanceAfter ?: "null"},")
            sb.append("\"txnTime\":${t.txnTime},")
            sb.append("\"rawBody\":${q(t.rawBody)},")
            sb.append("\"fingerprint\":${q(t.fingerprint)},")
            sb.append("\"isExcluded\":${t.isExcluded},")
            sb.append("\"exclusionReason\":${qn(t.exclusionReason?.name)},")
            sb.append("\"exclusionSource\":${q(t.exclusionSource.name)},")
            sb.append("\"note\":${q(t.note)},")
            sb.append("\"tags\":${q(t.tags)},")
            sb.append("\"isManual\":${t.isManual},")
            sb.append("\"isInternational\":${t.isInternational}")
            sb.append("}")
            if (idx < transactions.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        return sb.toString()
    }

    fun importBackupJson(json: String): Pair<List<TransactionEntity>, RulesBundle> {
        val root = JsonMiniParser(json).parseObject()
        val rulesMap = root["rules"] as? Map<*, *>
        val rulesBundle = if (rulesMap != null) {
            importRulesMap(rulesMap)
        } else {
            RulesBundle(emptyList(), emptyList(), emptyList(), emptyList())
        }

        val txns = (root["transactions"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            TransactionEntity(
                sender = m["sender"] as? String ?: "MANUAL",
                bankName = m["bankName"] as? String ?: "Cash",
                accountLast4 = m["accountLast4"] as? String,
                amount = (m["amount"] as? Number)?.toDouble() ?: return@mapNotNull null,
                currency = m["currency"] as? String ?: "₹",
                type = runCatching { TransactionType.valueOf(m["type"] as? String ?: "DEBIT") }.getOrDefault(TransactionType.DEBIT),
                merchant = m["merchant"] as? String ?: "Unknown",
                rawMerchant = m["rawMerchant"] as? String ?: (m["merchant"] as? String ?: "Unknown"),
                categoryId = (m["categoryId"] as? Number)?.toLong() ?: 22L,
                categorySource = runCatching { CategorySource.valueOf(m["categorySource"] as? String ?: "BUILTIN") }.getOrDefault(CategorySource.BUILTIN),
                channel = runCatching { TransactionChannel.valueOf(m["channel"] as? String ?: "OTHER") }.getOrDefault(TransactionChannel.OTHER),
                refNo = m["refNo"] as? String,
                balanceAfter = (m["balanceAfter"] as? Number)?.toDouble(),
                txnTime = (m["txnTime"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                rawBody = m["rawBody"] as? String ?: "",
                fingerprint = m["fingerprint"] as? String ?: return@mapNotNull null,
                isExcluded = m["isExcluded"] as? Boolean ?: false,
                exclusionReason = (m["exclusionReason"] as? String)?.let { runCatching { ExclusionReason.valueOf(it) }.getOrNull() },
                exclusionSource = runCatching { ExclusionSource.valueOf(m["exclusionSource"] as? String ?: "NONE") }.getOrDefault(ExclusionSource.NONE),
                note = m["note"] as? String ?: "",
                tags = m["tags"] as? String ?: "",
                isManual = m["isManual"] as? Boolean ?: false,
                isInternational = m["isInternational"] as? Boolean ?: false
            )
        } ?: emptyList()

        return txns to rulesBundle
    }

    private fun importRulesMap(root: Map<*, *>): RulesBundle {
        // Reuse importRulesJson structure via helper
        val catRules = (root["categoryRules"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            CategoryRuleEntity(
                name = m["name"] as? String ?: "Imported Rule",
                priority = (m["priority"] as? Number)?.toInt() ?: 100,
                categoryId = (m["categoryId"] as? Number)?.toLong() ?: 22L,
                merchantPattern = m["merchantPattern"] as? String,
                merchantMatchMode = m["merchantMatchMode"] as? String ?: "CONTAINS",
                bodyKeyword = m["bodyKeyword"] as? String,
                senderPattern = m["senderPattern"] as? String,
                vpaPattern = m["vpaPattern"] as? String,
                minAmount = (m["minAmount"] as? Number)?.toDouble(),
                maxAmount = (m["maxAmount"] as? Number)?.toDouble(),
                direction = (m["direction"] as? String)?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() },
                regexPattern = m["regexPattern"] as? String,
                alsoExclude = m["alsoExclude"] as? Boolean ?: false,
                exclusionReason = (m["exclusionReason"] as? String)?.let { runCatching { ExclusionReason.valueOf(it) }.getOrNull() },
                enabled = m["enabled"] as? Boolean ?: true
            )
        } ?: emptyList()

        val exclRules = (root["exclusionRules"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val reasonStr = m["reason"] as? String ?: ExclusionReason.OTHER.name
            ExclusionRuleEntity(
                name = m["name"] as? String ?: "Imported Exclusion",
                merchantPattern = m["merchantPattern"] as? String,
                bodyKeyword = m["bodyKeyword"] as? String,
                regexPattern = m["regexPattern"] as? String,
                exactAmount = (m["exactAmount"] as? Number)?.toDouble(),
                reason = runCatching { ExclusionReason.valueOf(reasonStr) }.getOrDefault(ExclusionReason.OTHER),
                enabled = m["enabled"] as? Boolean ?: true
            )
        } ?: emptyList()

        val aliases = (root["aliases"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val rawPat = m["rawPattern"] as? String ?: return@mapNotNull null
            val canon = m["canonicalName"] as? String ?: return@mapNotNull null
            MerchantAliasEntity(rawPattern = rawPat, canonicalName = canon, defaultCategoryId = (m["defaultCategoryId"] as? Number)?.toLong())
        } ?: emptyList()

        val templates = (root["templates"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val regex = m["regexPattern"] as? String ?: return@mapNotNull null
            ParseTemplateEntity(
                name = m["name"] as? String ?: "Imported Template",
                senderPattern = m["senderPattern"] as? String ?: "",
                regexPattern = regex,
                amountGroup = (m["amountGroup"] as? Number)?.toInt() ?: 1,
                merchantGroup = (m["merchantGroup"] as? Number)?.toInt(),
                accountGroup = (m["accountGroup"] as? Number)?.toInt(),
                balanceGroup = (m["balanceGroup"] as? Number)?.toInt(),
                defaultDirection = (m["defaultDirection"] as? String)?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() },
                enabled = m["enabled"] as? Boolean ?: true
            )
        } ?: emptyList()

        return RulesBundle(catRules, exclRules, aliases, templates)
    }

    private fun q(s: String): String {
        val sb = StringBuilder("\"")
        for (c in s) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (c < ' ') sb.append("\\u%04x".format(c.code)) else sb.append(c)
            }
        }
        sb.append("\"")
        return sb.toString()
    }

    private fun qn(s: String?): String = if (s == null) "null" else q(s)
}

private class JsonMiniParser(private val src: String) {
    private var idx = 0

    fun parseObject(): Map<String, Any?> {
        skipWs()
        expect('{')
        val map = linkedMapOf<String, Any?>()
        skipWs()
        if (peek() == '}') {
            idx++
            return map
        }
        while (true) {
            skipWs()
            val key = parseString()
            skipWs()
            expect(':')
            val value = parseValue()
            map[key] = value
            skipWs()
            when (peek()) {
                ',' -> idx++
                '}' -> {
                    idx++
                    return map
                }
                else -> throw IllegalArgumentException("Unexpected char '${peek()}' at $idx")
            }
        }
    }

    private fun parseArray(): List<Any?> {
        expect('[')
        val list = mutableListOf<Any?>()
        skipWs()
        if (peek() == ']') {
            idx++
            return list
        }
        while (true) {
            list.add(parseValue())
            skipWs()
            when (peek()) {
                ',' -> idx++
                ']' -> {
                    idx++
                    return list
                }
                else -> throw IllegalArgumentException("Unexpected char '${peek()}' in array at $idx")
            }
        }
    }

    private fun parseValue(): Any? {
        skipWs()
        return when (val c = peek()) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> parseString()
            't' -> { expectWord("true"); true }
            'f' -> { expectWord("false"); false }
            'n' -> { expectWord("null"); null }
            '-', in '0'..'9' -> parseNumber()
            else -> throw IllegalArgumentException("Unexpected value '$c' at $idx")
        }
    }

    private fun parseString(): String {
        expect('"')
        val sb = StringBuilder()
        while (idx < src.length) {
            val c = src[idx++]
            if (c == '"') return sb.toString()
            if (c == '\\') {
                val esc = src[idx++]
                when (esc) {
                    '"', '\\', '/' -> sb.append(esc)
                    'b' -> sb.append('\b')
                    'f' -> sb.append('\u000C')
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    't' -> sb.append('\t')
                    'u' -> {
                        val hex = src.substring(idx, idx + 4)
                        idx += 4
                        sb.append(hex.toInt(16).toChar())
                    }
                    else -> sb.append(esc)
                }
            } else {
                sb.append(c)
            }
        }
        throw IllegalArgumentException("Unterminated string")
    }

    private fun parseNumber(): Number {
        val start = idx
        if (src[idx] == '-') idx++
        while (idx < src.length && (src[idx].isDigit() || src[idx] == '.' || src[idx] == 'e' || src[idx] == 'E' || src[idx] == '+' || src[idx] == '-')) {
            idx++
        }
        val token = src.substring(start, idx)
        return if (token.contains('.') || token.contains('e', true)) token.toDouble() else token.toLong()
    }

    private fun expect(ch: Char) {
        if (idx >= src.length || src[idx] != ch) {
            throw IllegalArgumentException("Expected '$ch' at $idx")
        }
        idx++
    }

    private fun expectWord(word: String) {
        if (!src.startsWith(word, idx)) throw IllegalArgumentException("Expected $word at $idx")
        idx += word.length
    }

    private fun peek(): Char = if (idx < src.length) src[idx] else '\u0000'

    private fun skipWs() {
        while (idx < src.length && src[idx].isWhitespace()) idx++
    }
}
