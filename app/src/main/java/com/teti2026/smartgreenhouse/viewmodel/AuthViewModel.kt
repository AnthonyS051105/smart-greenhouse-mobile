package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.data.model.UserRole
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/** Sesuai pola `UiState` di `docs/SDD.md §5`, dikhususkan untuk hasil aksi Login/Register. */
sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState

    /**
     * [hasFarmSetup] hanya relevan untuk [UserRole.FARMER] — menentukan apakah caller (NavGraph)
     * mengarahkan ke Dashboard (sudah punya `farms`) atau flow Setup Greenhouse (belum). Selalu
     * `true` untuk [UserRole.BUYER] (tidak relevan, tidak ada flow setup lahan sisi Pembeli) dan
     * untuk hasil [register] (akun BARU petani otomatis belum pernah setup lahan → caller yang
     * memutuskan lewat nilai `false` di sini, bukan asumsi terpisah).
     */
    data class Success(val role: UserRole, val hasFarmSetup: Boolean) : AuthUiState
    data class Error(@param:StringRes val messageResId: Int) : AuthUiState
}

/** Sesuai pola `UiState` di `docs/SDD.md §5`, TERPISAH dari [AuthUiState] — dialog "Lupa Sandi" adalah aksi berdiri sendiri, tidak mengubah hasil Login/Register. */
sealed interface ForgotPasswordUiState {
    data object Idle : ForgotPasswordUiState
    data object Loading : ForgotPasswordUiState
    data object Success : ForgotPasswordUiState
    data class Error(@param:StringRes val messageResId: Int) : ForgotPasswordUiState
}

class AuthViewModel @JvmOverloads constructor(
    private val repository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordUiState> = _forgotPasswordState.asStateFlow()

    fun register(name: String, email: String, password: String, role: UserRole) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            repository.register(name, email, password, role)
                .onSuccess { user ->
                    // Akun baru petani belum mungkin punya dokumen farms — selalu false, tanpa
                    // perlu query hasFarmSetup (pasti kosong untuk uid yang baru saja dibuat).
                    _state.value = AuthUiState.Success(user.role, hasFarmSetup = false)
                }
                .onFailure { _state.value = AuthUiState.Error(mapAuthError(it)) }
        }
    }

    fun login(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            repository.login(email, password)
                .mapCatching { firebaseUser ->
                    val uid = firebaseUser.uid
                    val role = repository.fetchRole(uid).getOrThrow()
                    val hasFarmSetup = if (role == UserRole.FARMER) {
                        firestoreRepository.hasFarmSetup(uid).getOrThrow()
                    } else {
                        true
                    }
                    role to hasFarmSetup
                }
                .onSuccess { (role, hasFarmSetup) -> _state.value = AuthUiState.Success(role, hasFarmSetup) }
                .onFailure { _state.value = AuthUiState.Error(mapAuthError(it)) }
        }
    }

    /** Dipanggil UI setelah pesan error ditampilkan, supaya tidak muncul berulang saat recomposition. */
    fun consumeError() {
        if (_state.value is AuthUiState.Error) _state.value = AuthUiState.Idle
    }

    /**
     * Kirim email reset password ([AuthRepository.sendPasswordResetEmail]). Validasi format email
     * KASAR dilakukan client-side dulu (cek `contains('@')`, bukan regex penuh — Firebase sendiri
     * yang jadi validator utama lewat `ERROR_INVALID_EMAIL`) supaya tidak perlu panggil jaringan
     * untuk kasus jelas-jelas kosong/salah format.
     */
    fun sendPasswordReset(email: String) {
        if (!email.contains('@')) {
            _forgotPasswordState.value = ForgotPasswordUiState.Error(R.string.auth_error_invalid_email)
            return
        }
        _forgotPasswordState.value = ForgotPasswordUiState.Loading
        viewModelScope.launch {
            repository.sendPasswordResetEmail(email)
                .onSuccess { _forgotPasswordState.value = ForgotPasswordUiState.Success }
                .onFailure { _forgotPasswordState.value = ForgotPasswordUiState.Error(mapAuthError(it)) }
        }
    }

    /** Reset state dialog "Lupa Sandi" ke [ForgotPasswordUiState.Idle] — dipanggil saat dialog ditutup/dibuka ulang. */
    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordUiState.Idle
    }

    @StringRes
    private fun mapAuthError(throwable: Throwable): Int {
        // errorCode dibaca via FirebaseAuthException, BUKAN pattern-match subclass — SDK Firebase
        // Auth terbaru menyatukan beberapa kasus (mis. salah password vs akun tidak ada) menjadi
        // satu errorCode "ERROR_INVALID_CREDENTIAL" demi mencegah user enumeration, jadi subclass
        // exception saja tidak cukup granular.
        val errorCode = (throwable as? FirebaseAuthException)?.errorCode
        return when (errorCode) {
            "ERROR_INVALID_EMAIL" -> R.string.auth_error_invalid_email
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> R.string.auth_error_wrong_password
            "ERROR_USER_NOT_FOUND" -> R.string.auth_error_user_not_found
            "ERROR_EMAIL_ALREADY_IN_USE" -> R.string.auth_error_email_in_use
            "ERROR_WEAK_PASSWORD" -> R.string.auth_error_weak_password
            else -> if (throwable is IOException) R.string.auth_error_network else R.string.auth_error_generic
        }
    }
}
