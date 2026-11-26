# Bank SMS Expenses (Android)

An offline-first Android app (Kotlin + Jetpack Compose + Material 3 + Room) that reads bank transaction SMS messages on-device, automatically groups expenses **for each bank separately**, and provides a **consolidated All Banks view**.

## Features

- **All Banks Combined View**: Total debits, credits/inflows, net outflow, and category breakdown across all banks.
- **Per-Bank Separate Expenses**: Automatically classifies messages by bank (HDFC, ICICI, SBI, Axis, Kotak, IDFC FIRST, IndusInd, Yes Bank, PNB, BoB, Federal, Canara, Union, Standard Chartered, Citi, Amex, HSBC, Chase, BofA, Wells Fargo, plus dynamic fallback for any other bank sender). Tap any bank card to scope the category breakdown and transaction ledger to that bank.
- **On-Device SMS Sync & Live Receiver**: Reads existing inbox messages via `ContentResolver` (`READ_SMS`) and listens for new bank SMS in real time (`RECEIVE_SMS`).
- **Smart Filtering**: Ignores OTPs, promotional messages, reminders, and failed/declined alerts. Deduplicates synced messages using a deterministic SHA-256 fingerprint.
- **Category Breakdown & Overrides**: Automatically categorizes merchants (Food, Shopping, Transport, Bills, Entertainment, Health, Transfers/UPI, ATM, Other) and lets you inspect the original SMS or re-categorize any transaction.
- **Built-in Sample Data**: Includes a "Load Samples" / "Try Sample Data" mode so you can preview the app on an emulator or device without granting SMS access first.
