package com.teti2026.smartgreenhouse.util

import java.text.NumberFormat
import java.util.Locale

/** "Rp 25.000" — dipakai layar manapun yang menampilkan `price_per_kg`/`total_price` (data-contracts.md §3). */
fun formatRupiah(amount: Long): String {
    val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(amount)
    return "Rp $formatted"
}

/** "Rp 25.000/kg" — padanan [formatRupiah] khusus kartu/daftar listing (`price_per_kg`). */
fun formatRupiahPerKg(amount: Long): String = "${formatRupiah(amount)}/kg"
