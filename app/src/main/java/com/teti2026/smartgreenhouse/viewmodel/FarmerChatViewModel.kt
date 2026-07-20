package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.farmer.chat.FarmerChatConversation
import com.teti2026.smartgreenhouse.ui.farmer.chat.FarmerChatMessageItem
import com.teti2026.smartgreenhouse.util.chatThreadId
import com.teti2026.smartgreenhouse.util.parseChatThreadId
import com.teti2026.smartgreenhouse.util.toFarmerChatMessageItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface FarmerChatUiState {
    data object Loading : FarmerChatUiState
    data class Success(val conversation: FarmerChatConversation, val messages: List<FarmerChatMessageItem>) : FarmerChatUiState
    data class Error(@param:StringRes val messageResId: Int) : FarmerChatUiState
}

/**
 * Padanan [ChatViewModel] untuk sisi Petani. [conversationId] yang diterima [load] adalah thread
 * id APA ADANYA (`chatThreadId(listingId, buyerUid)`, lihat `util/ChatThreadId.kt`) — dibangun
 * oleh [FarmerChatListViewModel] saat membuat [FarmerChatConversation.id], BUKAN id sample lama
 * ("conv-1" dkk). `listingId`/`buyerUid` diurai balik dari [conversationId] via
 * [parseChatThreadId] — petani (penjual) SELALU dirinya sendiri (uid dari
 * [AuthRepository.currentUser]), TIDAK perlu di-resolve dari data.
 */
class FarmerChatViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<FarmerChatUiState>(FarmerChatUiState.Loading)
    val state: StateFlow<FarmerChatUiState> = _state.asStateFlow()

    private var listingId: String? = null
    private var buyerUid: String? = null
    // Lihat catatan loadJob di ChatViewModel — mencegah listener realtime menumpuk bila load()
    // dipanggil ulang.
    private var loadJob: Job? = null

    fun load(conversationId: String) {
        val uid = authRepository.currentUser()?.uid
        val parsed = parseChatThreadId(conversationId)
        if (uid == null || parsed == null) {
            _state.value = FarmerChatUiState.Error(R.string.chat_error_load_failed)
            return
        }
        val (listingId, buyerUid) = parsed
        this.listingId = listingId
        this.buyerUid = buyerUid
        _state.value = FarmerChatUiState.Loading

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val listing = firestoreRepository.getListingById(listingId).getOrNull()
            if (listing == null) {
                _state.value = FarmerChatUiState.Error(R.string.chat_error_load_failed)
                return@launch
            }
            val buyer = firestoreRepository.getUser(buyerUid).getOrNull()
            val conversation = FarmerChatConversation(
                id = conversationId,
                listingId = listingId,
                buyerName = buyer?.name.orEmpty(),
                buyerAvatarUrl = null,
                cropName = listing.productName.ifBlank { listing.cropType },
                lastMessage = "",
                lastMessageTimeLabel = ""
            )
            firestoreRepository.getMessagesFlow(conversationId, uid).collect { messages ->
                _state.value = FarmerChatUiState.Success(
                    conversation = conversation,
                    messages = messages.map { it.toFarmerChatMessageItem(myUid = uid) }
                )
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val uid = authRepository.currentUser()?.uid ?: return
        val listingId = this.listingId ?: return
        val buyer = buyerUid ?: return
        val threadId = chatThreadId(listingId, buyer)
        viewModelScope.launch {
            firestoreRepository.sendMessage(threadId, listingId, uid, buyer, trimmed)
        }
    }
}
