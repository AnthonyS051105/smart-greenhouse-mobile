package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.ChatMessageItem
import com.teti2026.smartgreenhouse.ui.buyer.ListingDetailItem
import com.teti2026.smartgreenhouse.util.chatThreadId
import com.teti2026.smartgreenhouse.util.toChatMessageItem
import com.teti2026.smartgreenhouse.util.toListingDetailItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface ChatUiState {
    data object Loading : ChatUiState
    data class Success(val product: ListingDetailItem, val messages: List<ChatMessageItem>) : ChatUiState
    data class Error(@param:StringRes val messageResId: Int) : ChatUiState
}

/**
 * Chat Negosiasi sisi Pembeli untuk satu [listingId] — pembeli SELALU dirinya sendiri (uid dari
 * [AuthRepository.currentUser]), penjual di-resolve dari `listings.farm_id` → `farms.owner_uid`
 * (padanan [ListingDetailViewModel]). Thread id = `chatThreadId(listingId, myUid)`, lihat
 * `util/ChatThreadId.kt`. [load] memasang listener REALTIME (`FirestoreRepository.getMessagesFlow`)
 * yang tetap aktif selama ViewModel hidup — bukan sekali baca seperti kebanyakan ViewModel lain di
 * app ini, karena chat butuh update pesan baru tanpa refresh manual (SRS MOB-FR-17).
 */
class ChatViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var listingId: String? = null
    private var sellerUid: String? = null
    // Melacak coroutine listener realtime yang sedang aktif supaya bisa dibatalkan sebelum
    // memasang listener baru — mencegah dua listener menumpuk (leak) bila load() dipanggil ulang
    // (mis. tombol "Coba Lagi" di ProfileErrorView) sementara listener sebelumnya masih hidup.
    private var loadJob: Job? = null

    fun load(listingId: String) {
        val uid = authRepository.currentUser()?.uid
        if (uid == null) {
            _state.value = ChatUiState.Error(R.string.auth_error_generic)
            return
        }
        this.listingId = listingId
        _state.value = ChatUiState.Loading
        val threadId = chatThreadId(listingId, uid)

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val product = firestoreRepository.getListingById(listingId)
                .mapCatching { listing ->
                    val farm = firestoreRepository.getFarmById(listing.farmId).getOrNull()
                    sellerUid = farm?.ownerUid
                    val seller = farm?.let { firestoreRepository.getUser(it.ownerUid).getOrNull() }
                    listing.toListingDetailItem(farm, seller)
                }
                .getOrNull()
            if (product == null) {
                _state.value = ChatUiState.Error(R.string.chat_error_load_failed)
                return@launch
            }
            firestoreRepository.getMessagesFlow(threadId, uid).collect { messages ->
                _state.value = ChatUiState.Success(
                    product = product,
                    messages = messages.map { it.toChatMessageItem(myUid = uid) }
                )
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val uid = authRepository.currentUser()?.uid ?: return
        val listingId = this.listingId ?: return
        val seller = sellerUid ?: return
        val threadId = chatThreadId(listingId, uid)
        viewModelScope.launch {
            firestoreRepository.sendMessage(threadId, listingId, uid, seller, trimmed)
        }
    }
}
