package com.teti2026.smartgreenhouse.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

/** "24 Okt 2023" dari string ISO 8601 UTC (`created_at` dsb, data-contracts.md §6) — dipakai kartu Riwayat Pesanan. */
fun formatOrderDate(isoInstant: String): String {
    val instant = runCatching { Instant.parse(isoInstant) }.getOrNull() ?: return isoInstant
    return SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date.from(instant))
}
