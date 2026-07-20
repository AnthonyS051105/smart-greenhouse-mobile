package com.teti2026.smartgreenhouse.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teti2026.smartgreenhouse.R
import com.teti2026.smartgreenhouse.repository.AuthRepository
import com.teti2026.smartgreenhouse.repository.FirestoreRepository
import com.teti2026.smartgreenhouse.ui.buyer.BuyerChatConversation
import com.teti2026.smartgreenhouse.util.relativeDayOrTimeLabel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/** Sesuai pola `UiState` di `docs/SDD.md §5`. */
sealed interface ChatListUiState {
    data object Loading : ChatListUiState
    data class Success(val conversations: List<BuyerChatConversation>) : ChatListUiState
    data class Error(@param:StringRes val messageResId: Int) : ChatListUiState
}

/**
 * Daftar percakapan sisi Pembeli ("Pesan - Pembeli"). Sumber: listener REALTIME seluruh
 * `chat_messages` yang melibatkan pembeli yang login ([FirestoreRepository.getMessagesForUserFlow])
 * — dikelompokkan client-side per `thread_id`, ambil pesan TERBARU per grup (diurut by `sentAt`
 * MENTAH SEBELUM diformat — bukan sesudah, supaya urutan tetap kronologis benar, bukan urutan
 * teks label "Kemarin"/"N hari lalu"). Realtime (bukan sekali baca) supaya baris "pesan terakhir"
 * ikut ter-update live saat penjual membalas TANPA perlu keluar-masuk layar — laporan user pada
 * versi sebelumnya (sekali baca) persis gap ini. [BuyerChatConversation.unreadCount] SELALU 0 —
 * belum ada sistem status baca/read-receipt (gap yang sama seperti [FarmerChatListViewModel]).
 */
class ChatListViewModel @JvmOverloads constructor(
    private val authRepository: AuthRepository = AuthRepository(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val state: StateFlow<ChatListUiState> = _state.asStateFlow()

    // Mencegah listener menumpuk bila load() dipanggil ulang (mis. tombol "Coba Lagi") sementara
    // listener sebelumnya masih hidup — lihat catatan sama di ChatViewModel.
    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        val uid = authRepository.currentUser()?.uid
        if (uid == null) {
            _state.value = ChatListUiState.Error(R.string.auth_error_generic)
            return
        }
        _state.value = ChatListUiState.Loading
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            firestoreRepository.getMessagesForUserFlow(uid)
                .catch { _state.value = ChatListUiState.Error(R.string.chat_list_error_load_failed) }
                .collect { messages ->
                    val conversations = messages.groupBy { it.threadId }.values
                        .mapNotNull { threadMessages -> threadMessages.maxByOrNull { it.sentAt } }
                        .sortedByDescending { it.sentAt }
                        .mapNotNull { latest ->
                            val listing = firestoreRepository.getListingById(latest.listingId).getOrNull()
                                ?: return@mapNotNull null
                            val farm = firestoreRepository.getFarmById(listing.farmId).getOrNull()
                            val seller = farm?.let { firestoreRepository.getUser(it.ownerUid).getOrNull() }
                            BuyerChatConversation(
                                id = latest.threadId,
                                listingId = latest.listingId,
                                sellerName = seller?.name.orEmpty(),
                                sellerAvatarUrl = null,
                                cropName = listing.productName.ifBlank { listing.cropType },
                                lastMessage = latest.message,
                                lastMessageTimeLabel = relativeDayOrTimeLabel(latest.sentAt),
                                unreadCount = 0
                            )
                        }
                    _state.value = ChatListUiState.Success(conversations)
                }
        }
    }
}
