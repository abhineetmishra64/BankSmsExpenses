# SMS Expense Tracker – Android App Spec

**Version:** 1.0 (draft) **Platform:** Android (min SDK 26, target latest) **Mode:** 100% offline, on-device

---

## 1. Overview

An Android app that reads bank and credit-card transaction SMS messages on the device, parses them with a rule-based engine, categorises them, and presents monthly summaries and trends. Nothing leaves the phone: no network permission, no cloud, no analytics, no ML model downloads.

### Goals

- Zero-effort tracking: transactions appear automatically as SMS arrive.
- Accurate totals: the user can exclude non-expenses (investments, self-transfers, card bill payments) and the app remembers the decision.
- Transparent categorisation: every category assignment comes from a rule the user can see, edit and override.
- Complete privacy: all data stays local; the app cannot access the internet at all.

### Non-goals (v1)

- Cloud sync, multi-device sync, bank API/Account Aggregator integration.
- Reading email, PDF statements or notifications (possible later extensions).
- ML-based categorisation (rules only, by design).

---

## 2. Key Principles

1. **Offline by construction.** The manifest does not declare `INTERNET` or `ACCESS_NETWORK_STATE`. No third-party SDK that phones home. This is verifiable by the user in Android's app info screen.
2. **Rules, not magic.** Parsing, categorisation and exclusion are all deterministic rules stored in the local DB. The user can inspect and change every one.
3. **User decisions are sticky.** A manual correction is remembered and applied to similar past and future transactions, and always wins over built-in rules.
4. **Never lose raw data.** The original SMS text is stored with each transaction, so parsing can be re-run when rules improve.

---

## 3. Functional Requirements

### 3.1 Main Screen – Current Month Summary

**Purpose:** at-a-glance view of the current month.

**Contents**

- **Month header** with previous/next arrows (defaults to current month; user can step back to review earlier months).
- **Account/Card selector** (dropdown or horizontally scrollable chips): *All accounts*, then each detected bank account (e.g. "HDFC Savings ••1234") and card (e.g. "ICICI Credit ••5678"). Multi-select is supported (e.g. pick two accounts).
- **Summary cards:**
  - Total expense (excludes items marked non-expense)
  - Total income
  - Net (income − expense)
  - Excluded amount (small tappable line: "₹42,000 excluded" → opens the Exclusions screen filtered to this month)
- **Category breakdown:** donut chart plus ranked list with amount, % of total and transaction count. Tapping a category opens the transaction list filtered to it.
- **Top merchants** for the month (top 5).
- **Recent transactions** (last 10) with a "View all" link.
- **Budget progress bars** (if budgets are set – see 4.1).
- **Needs attention badge:** count of uncategorised transactions and unparsed messages.

**Behaviour**

- Totals respond instantly to account filter changes.
- Account filter selection persists across app restarts.
- Credit card spends count as expense on the transaction date (not on bill payment). The later card bill payment from the bank account is auto-flagged as a *transfer* and excluded to avoid double counting (see 3.3).

### 3.2 Trend View

**Purpose:** see how income and expense evolve over time.

**Contents**

- **Grouped bar chart:** income vs expense per month; default range last 12 months; range selector (3M / 6M / 12M / FY / custom).
- **Net savings line** overlaid on the chart (toggle).
- **Account/Card filter** identical to the main screen (shared component).
- **Category filter:** optionally view the trend of a single category (e.g. Food over 12 months).
- **Tap a bar** to see that month's summary below the chart (income, expense, top categories) with a button to open that month on the main screen.
- **Table view toggle:** month, income, expense, net, savings rate %.
- **Insights strip:** average monthly expense, highest-spend month, month-over-month change.
- Optional **by category stacked bars** mode.

### 3.3 Non-Expense / Exclusion Management

**Purpose:** keep summaries meaningful by excluding things that are not real spending or income, such as SIP/mutual fund deductions, stock purchases, loan principal repayments, transfers between own accounts, credit card bill payments, and refunded transactions.

**Marking an item**

- From any transaction (detail screen or swipe action): **"Don't count in summary"** with a reason picker (Investment, Self-transfer, Card bill payment, Loan EMI principal, Reimbursable, Other).
- After marking, the app offers: **"Apply to similar transactions?"** and shows a preview list of matches (past and future). Options:
  - This transaction only
  - All from this merchant/beneficiary
  - All matching a pattern (e.g. text contains "ACH D-" and "MUTUAL FUND")
  - Same amount + same merchant (good for recurring SIPs)

**Memory (exclusion rules)**

- Each choice creates an **Exclusion Rule** (merchant, keyword/regex, optional amount, optional account, reason).
- Rules apply automatically to new incoming SMS and optionally retroactively.
- Priority: manual per-transaction override > user rules > built-in rules.

**Exclusions screen (new view)**

- Tab 1 **Excluded transactions:** all currently excluded items, grouped by reason and month, with total. Select one or many → **Include again**.
- Tab 2 **Rules:** list of exclusion rules with match count, last matched date, enable/disable toggle, edit, delete. "Reset" a rule to re-include all matches in one tap.
- Tab 3 **Suggestions:** the app proposes exclusions based on built-in heuristics (e.g. messages mentioning "SIP", "mutual fund", "NACH", "credit card payment", "self transfer", "FD", "PPF", "NPS"). The user accepts or dismisses each; dismissals are remembered.
- **Bulk action:** search messages by text and exclude/include the whole set.
- Excluded items never appear in totals, charts or budgets, but are always visible in the full transaction list (greyed with a badge).

**Auto-detected transfers**

- If a debit on one of the user's accounts and a credit of the same amount on another of the user's accounts occur within a configurable window (default 10 minutes), flag both as *internal transfer* and exclude them. User can undo.
- Card bill payment: a debit mentioning a card bill/payment plus a matching credit on the card is treated the same way.

### 3.4 Categorisation (Rule-Based, Offline)

**Default categories:** Food & Dining, Groceries, Transport & Fuel, Travel, Hotels & Stays, Shopping, Bills & Utilities, Recharge & Subscriptions, Rent, EMI & Loans, Insurance, Health, Education, Entertainment, Cash Withdrawal, Fees & Charges, Salary/Income, Interest, Refund, Transfers (personal), Investments, Other. Users can add, rename, recolour, merge or delete categories, and nest sub-categories (optional).

**Rule engine**

- A rule = conditions + category (+ optional flags such as "exclude from summary").
- **Conditions:** merchant name contains/equals; SMS body keyword(s); sender ID; UPI VPA pattern; amount range; account/card; direction (debit/credit); regex (advanced).
- **Evaluation order** (first match wins):
  1. Per-transaction manual override
  2. User rules (ordered, drag to reprioritise)
  3. Merchant alias table (e.g. "SWIGGY\*ORDER" → Swiggy → Food & Dining)
  4. Built-in keyword dictionary (bundled JSON: electricity, DTH, jio, airtel, irctc, makemytrip, uber, ola, zomato, bigbasket, petrol, pharmacy, etc.)
  5. Fallback: *Uncategorised*
- Built-in dictionary ships in the APK and can be updated only by app updates or by importing a JSON rule file from storage (no internet).

**Correcting a category**

- Tap a transaction → change category → prompt: **"Always categorise \[Merchant\] as \[Category\]?"** with options: this one only / all past and future from this merchant / create custom rule.
- Shows count of affected past transactions before applying.
- Corrections are stored as user rules and appear in **Settings → Rules** where they can be edited or removed.
- **Uncategorised inbox:** a queue of transactions the engine could not classify, grouped by merchant so that one tap categorises many at once.

**Rule manager**

- List, search, test (paste sample SMS and see which rule fires), reorder, import/export as JSON.

### 3.5 Local-Only Storage and Offline Operation

- All data lives in an on-device Room (SQLite) database in app-private storage.
- Optional database encryption with SQLCipher, key protected by Android Keystore.
- No `INTERNET` permission; no network libraries; Android auto-backup disabled by default (`allowBackup=false`) so data is not silently uploaded to Google Drive. User can opt in to manual encrypted export (see 4.6).
- App works fully in airplane mode.
- Uninstalling the app removes all data (documented in Settings).

---

## 4. Additional Recommended Features

### 4.1 Budgets

Monthly budget per category and overall, with progress bars on the home screen and a local notification at 80% and 100%.

### 4.2 Transaction List and Search

Full list with filters (date range, account, category, amount range, direction, excluded/included, uncategorised) and free-text search across merchant and raw SMS. Group by day with daily totals.

### 4.3 Manual Transactions

Add cash expenses or transactions with no SMS (cash account). Edit amount, date, notes, tags. Deleting or editing an SMS-derived transaction keeps the raw SMS link.

### 4.4 Notes, Tags and Splits

Attach a note and tags (e.g. "trip-goa") to any transaction. Split one transaction across categories (e.g. a supermarket bill into Groceries and Health).

### 4.5 Recurring and Subscription Detection

Detect repeating merchant + similar amount at \~monthly intervals; list as "Recurring payments" with next expected date and a reminder for upcoming dues.

### 4.6 Backup and Restore

Manual export to an encrypted file (password-protected) in user-chosen storage via the system file picker, and import to restore. Also CSV export for spreadsheets and rule export/import as JSON.

### 4.7 Credit Card Features

Track outstanding, available limit (when present in SMS), statement due date and a reminder before due date. Show card utilisation if limit is known.

### 4.8 Balance Tracking

Capture "Avl Bal" from SMS to show last known balance per account and a low-balance alert threshold.

### 4.9 Unparsed Messages Queue

SMS from known bank senders that look financial but failed parsing go into a queue. The user can fix them via a simple template builder: highlight the amount, merchant and account in the SMS and the app generates a reusable parsing rule.

### 4.10 Security

App lock (biometric/PIN), hide amounts on the recents screen (`FLAG_SECURE`), and a "privacy mode" toggle that masks amounts on the home screen.

### 4.11 Misc Quality-of-Life

- Home screen widget with this month's expense and top category.
- Dark mode and Material You theming.
- Multi-currency display (store original currency; INR default) and international-transaction flag.
- Financial year (Apr–Mar) option for reports.
- Custom month start day (e.g. salary on the 25th → month runs 25th to 24th).
- Merchant alias manager (merge "AMZN PAY" and "Amazon").
- Yearly report screen and shareable PDF/CSV monthly report (generated locally).

---

## 5. SMS Ingestion

### 5.1 Permissions

- `READ_SMS` – initial import of historic messages.
- `RECEIVE_SMS` – real-time processing of new messages via a broadcast receiver.
- `POST_NOTIFICATIONS` – optional, for budget and due-date alerts.
- **No** `INTERNET`.

> **Distribution note:** Google Play restricts SMS permissions to apps whose core functionality needs them and requires a permissions declaration. An expense tracker is an accepted use case but review is strict. Alternatives: distribute via direct APK / F-Droid, or support a fallback where the user shares/imports SMS via an exported file. Decide on distribution before building.

### 5.2 Flow

1. **First run:** explain why SMS access is needed; request permissions; let the user choose how far back to import (1/3/6/12 months or all).
2. **Backfill:** a background worker (WorkManager) reads the SMS inbox via `Telephony.Sms.Inbox`, filters candidates, parses, deduplicates, categorises and stores.
3. **Live:** `SMS_RECEIVED` receiver hands off to a worker; the transaction appears within seconds.
4. **Re-scan:** manual "Re-process all messages" re-runs parsing and rules over stored raw SMS without touching user overrides.

### 5.3 Filtering

- Consider only messages from sender IDs matching bank/wallet/card patterns (e.g. `XX-HDFCBK`, `AX-ICICIB`) or with financial keywords (debited, credited, spent, withdrawn, UPI, a/c, card, INR/Rs).
- Ignore OTPs, promotions, balance-only alerts (store balance, no transaction), and failed/declined transaction messages.
- User can mark a sender as "ignore" or "include".

### 5.4 Deduplication

- Hash of (sender, timestamp, normalised body) is unique.
- Secondary check: same amount + account + reference number within 5 minutes treated as duplicate (banks often send duplicate alerts).

---

## 6. Parsing Engine

A layered, rule-based extractor.

**Fields extracted**

- Direction (debit / credit)
- Amount and currency
- Account or card identifier (last 4 digits) and type (savings, credit card, wallet)
- Merchant / counterparty / UPI VPA
- Transaction date/time (SMS timestamp as fallback)
- Reference number (UPI Ref, IMPS/NEFT ref)
- Available balance and credit limit (optional)
- Channel (UPI, card POS, ATM, NEFT/IMPS, NACH/ECS, net banking)

**Approach**

1. **Bank templates:** a bundled JSON/YAML library of regex templates per sender/bank with named capture groups. Easy to add new banks without code changes.
2. **Generic fallback:** keyword-anchored regexes (amount after "Rs"/"INR", account after "A/c", merchant after "to"/"at"/"info:").
3. **Confidence score** per parse; low confidence goes to the Unparsed/Review queue.
4. **Test harness:** a sample-SMS corpus with expected outputs, run as unit tests, so rule changes never regress.

**Edge cases to handle**

- Refunds and reversals (link to the original transaction where possible; net them off).
- Failed transactions followed by auto-reversal.
- Multiple amounts in one message (amount vs balance vs limit).
- Different amount formats (`1,23,456.78`, `Rs.500`, `INR 500.00`).
- Standing instructions, auto-debit/mandates, EMI conversions.
- Same account referred to with different masks (XX1234, \*1234, ending 1234).

---

## 7. Data Model (Room)

| Table | Key fields |
| --- | --- |
| `sms_raw` | id, sender, body, received\_at, hash, parse\_status |
| `account` | id, display\_name, bank, type (savings/credit/wallet/cash), last4, last\_balance, credit\_limit, is\_active |
| `transaction` | id, sms\_id (nullable), account\_id, amount, currency, direction, merchant\_id, txn\_time, channel, ref\_no, category\_id, category\_source (manual/rule/builtin), is\_excluded, exclusion\_reason, exclusion\_source, note, is\_manual |
| `merchant` | id, canonical\_name, default\_category\_id |
| `merchant_alias` | id, merchant\_id, raw\_pattern |
| `category` | id, name, icon, colour, parent\_id, is\_builtin |
| `category_rule` | id, priority, conditions (JSON), category\_id, enabled, match\_count |
| `exclusion_rule` | id, conditions (JSON), reason, enabled, apply\_retroactively, match\_count |
| `budget` | id, category\_id (null = overall), amount, period |
| `tag` / `transaction_tag` | many-to-many |
| `split` | transaction\_id, category\_id, amount |
| `parse_template` | id, sender\_pattern, regex, field\_map, is\_builtin |
| `settings` | key/value |

**Indexes:** (txn\_time), (account\_id, txn\_time), (category\_id, txn\_time), (is\_excluded). Monthly aggregates are computed via SQL queries; a materialised monthly summary table can be added if performance requires.

---

## 8. Screens and Navigation

Bottom navigation:

1. **Home** – monthly summary (3.1)
2. **Trends** – income/expense over time (3.2)
3. **Transactions** – searchable list (4.2)
4. **Review** – uncategorised, unparsed and suggested exclusions inbox
5. **More** – Exclusions (3.3), Categories and Rules (3.4), Accounts, Budgets, Recurring, Backup, Settings

Other screens: Transaction detail, Category rule editor, Exclusion rule editor, Template builder, Onboarding and permission flow, Account management (rename, merge, hide).

**Account management:** accounts are auto-created from SMS; user can rename ("Salary account"), merge duplicates, hide, and assign a type.

---

## 9. Technical Architecture

- **Language/UI:** Kotlin, Jetpack Compose, Material 3
- **Architecture:** MVVM with Clean Architecture layers (UI → ViewModel → UseCase → Repository → Room)
- **DI:** Hilt
- **DB:** Room (+ optional SQLCipher)
- **Background:** WorkManager for backfill/re-scan; `BroadcastReceiver` for live SMS
- **Charts:** Compose-native charts (Vico or custom Canvas)
- **Serialization:** kotlinx.serialization for rule JSON
- **Testing:** JUnit and parameterised tests for parser/categoriser using a sample-SMS corpus; Room in-memory tests; Compose UI tests for core flows
- **Modules (suggested):** `app`, `core-parser`, `core-rules`, `core-data`, `feature-home`, `feature-trends`, `feature-exclusions`, `feature-rules`, `feature-settings`. The parser and rule engine are pure Kotlin so they can be unit-tested without Android.

---

## 10. Non-Functional Requirements

| Area | Requirement |
| --- | --- |
| Privacy | No network permission; no analytics or crash reporting SDKs; data in app-private storage |
| Performance | Backfill of 10,000 SMS in under 60 seconds in the background; home screen loads under 500 ms |
| Battery | No persistent service; event-driven receiver and short workers |
| Reliability | Idempotent ingestion; re-scan never overwrites manual user decisions |
| Accuracy | At least 95% of messages from the top supported banks parsed correctly on the test corpus; category precision tracked in tests |
| Usability | Any correction is at most 3 taps; undo available for all bulk actions |
| Accessibility | TalkBack labels, large-text support, colour-blind-safe chart palette |
| Localisation | English first; locale-aware number and date formatting; lakh/crore grouping option |
| Compatibility | Android 8.0+ |

---

## 11. Suggested Milestones

1. **M1 – Foundations (2 wks):** project setup, Room schema, permissions flow, SMS backfill, raw storage.
2. **M2 – Parser (2–3 wks):** templates for top banks and cards, generic fallback, dedupe, test corpus, accounts auto-detection.
3. **M3 – Categorisation (2 wks):** built-in dictionary, rule engine, merchant aliases, correction flow, uncategorised inbox.
4. **M4 – Home and Transactions (2 wks):** monthly summary, account filter, transaction list and detail.
5. **M5 – Trends (1–2 wks):** charts, filters, table view.
6. **M6 – Exclusions (2 wks):** manual exclusion, rules and memory, auto-transfer detection, exclusions screen.
7. **M7 – Extras (3 wks):** budgets, recurring, backup/export, app lock, widget.
8. **M8 – Hardening (2 wks):** performance, edge-case SMS, accessibility, release prep.

---

## 12. Acceptance Criteria (Core)

- A new bank SMS received while the app is closed appears in the correct month, account and category within 10 seconds of opening the app (or immediately if the receiver fires).
- Switching the account filter on Home and Trends updates all numbers consistently; the sum across all accounts equals the "All accounts" total.
- Marking one SIP deduction as non-expense and choosing "apply to similar" excludes all matching past SMS and automatically excludes the next one received; totals change accordingly; the item remains visible in the Exclusions screen and can be reset.
- Recategorising a merchant once updates every transaction of that merchant and categorises future ones the same way; the rule is visible and editable in the rule manager.
- With airplane mode on and the `INTERNET` permission absent from the manifest, every feature works.
- Deleting a rule never deletes transactions; re-processing never overrides manual choices.

---

## 13. Open Questions

1. **Distribution:** Play Store (needs SMS permission declaration) or sideload/F-Droid?
2. **Banks and cards to support first:** list your banks and card issuers so templates are built and tested against real message formats.
3. **Month definition:** calendar month or custom start day (e.g. salary date)?
4. **Credit card accounting:** count spend at swipe time (recommended) or at bill payment?
5. **Database encryption:** is SQLCipher worth the added complexity and size?
6. **Languages:** any need for regional-language SMS parsing?
7. **Multi-user or shared household** use, or strictly single user?
