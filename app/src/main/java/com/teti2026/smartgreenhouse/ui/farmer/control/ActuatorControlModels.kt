package com.teti2026.smartgreenhouse.ui.farmer.control

/** Jenis aktuator yang dikontrol — persis nilai `actuator` di `docs/data-contracts.md §1.2`. */
enum class ActuatorControlType {
    IRRIGATION,
    VENTILATION
}

/** Mode kontrol yang ditampilkan lewat segmented control "Otomatis"/"Manual" pada kedua screen. */
enum class ActuatorControlMode {
    AUTOMATIC,
    MANUAL
}

/**
 * State layar Kontrol Irigasi/Ventilasi. [isOn] adalah padanan presentasi `irrigation_state`/
 * `ventilation_state` (`open`/`closed`, `docs/data-contracts.md §1.3`). [activeSinceLabel] hanya
 * relevan saat [mode] = [ActuatorControlMode.AUTOMATIC] dan [isOn] = true (caption "Aktif sejak
 * ... (AI)" pada desain Kontrol Ventilasi).
 */
data class ActuatorControlUiState(
    val mode: ActuatorControlMode,
    val isOn: Boolean,
    val activeSinceLabel: String? = null
)
