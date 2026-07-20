package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.farmer.chat.FarmerChatConversation
import com.teti2026.smartgreenhouse.util.parseChatThreadId
import com.teti2026.smartgreenhouse.util.relativeDayOrTimeLabel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface FarmerChatListUiState {
    data object Loading : FarmerChatListUiState
    data class Success(val conversations: List<FarmerChatConversation>) : FarmerChatListUiState
    data class Error(@param:StringRes val messageResId: Int) : FarmerChatListUiState
}

/**
 * Padanan [ChatListViewModel] untuk sisi Petani ("Pesan - Petani") — realtime, lihat KDoc
 * [ChatListViewModel] untuk alasan (gap "pesan terakhir tidak ter-update" yang dilaporkan user).
 * `buyerUid`+`listingId` per percakapan diurai langsung dari `thread_id` (via
 * [parseChatThreadId]) — lebih murah dari sisi Pembeli karena tidak perlu resolve penjual lewat
 * `farms` (petani = dirinya sendiri, sudah pasti).
 */
class FarmerChatListViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<FarmerChatListUiState>(FarmerChatListUiState.Loading)
    val state: StateFlow<FarmerChatListUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        val uid = authRepository.currentUser()?.uid
        if (uid == null) {
            _state.value = FarmerChatListUiState.Error(R.string.auth_error_generic)
            return
        }
        _state.value = FarmerChatListUiState.Loading
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            firestoreRepository.getMessagesForUserFlow(uid)
                .catch { _state.value = FarmerChatListUiState.Error(R.string.chat_list_error_load_failed) }
                .collect { messages ->
                    val conversations = messages.groupBy { it.threadId }.values
                        .mapNotNull { threadMessages -> threadMessages.maxByOrNull { it.sentAt } }
                        .sortedByDescending { it.sentAt }
                        .mapNotNull { latest ->
                            val (listingId, buyerUid) = parseChatThreadId(latest.threadId) ?: return@mapNotNull null
                            val listing = firestoreRepository.getListingById(listingId).getOrNull()
                                ?: return@mapNotNull null
                            val buyer = firestoreRepository.getUser(buyerUid).getOrNull()
                            FarmerChatConversation(
                                id = latest.threadId,
                                listingId = listingId,
                                buyerName = buyer?.name.orEmpty(),
                                buyerAvatarUrl = null,
                                cropName = listing.productName.ifBlank { listing.cropType },
                                lastMessage = latest.message,
                                lastMessageTimeLabel = relativeDayOrTimeLabel(latest.sentAt),
                                unreadCount = 0
                            )
                        }
                    _state.value = FarmerChatListUiState.Success(conversations)
                }
        }
    }
}
