package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.data.model.User
import com.teti2026.smartgreenhouse.data.model.UserRole
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`, dipakai layar Profil kedua sisi (Petani & Pembeli). */
sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    /** [farmName] hanya terisi untuk [UserRole.FARMER] (subjudul "Pemilik <farmName>"), selalu null untuk Pembeli. */
    data class Success(val user: User, val farmName: String?) : ProfileUiState
    data class Error(@param:StringRes val messageResId: Int) : ProfileUiState
}

/**
 * Dipakai bersama oleh [com.teti2026.smartgreenhouse.ui.farmer.ProfileFarmerRoute] dan
 * [com.teti2026.smartgreenhouse.ui.buyer.ProfileBuyerRoute] — keduanya butuh data `users/{uid}`
 * yang sama persis, hanya beda cara menampilkannya.
 */
class ProfileViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        val uid = authRepository.currentUser()?.uid
        if (uid == null) {
            _state.value = ProfileUiState.Error(R.string.auth_error_generic)
            return
        }
        _state.value = ProfileUiState.Loading
        viewModelScope.launch {
            firestoreRepository.getUser(uid)
                .mapCatching { user ->
                    val farmName = if (user.role == UserRole.FARMER) {
                        firestoreRepository.getFarmNameForOwner(uid).getOrNull()
                    } else {
                        null
                    }
                    user to farmName
                }
                .onSuccess { (user, farmName) -> _state.value = ProfileUiState.Success(user, farmName) }
                .onFailure { _state.value = ProfileUiState.Error(R.string.profile_error_load_failed) }
        }
    }
}
