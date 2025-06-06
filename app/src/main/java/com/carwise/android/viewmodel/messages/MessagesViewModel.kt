package com.carwise.android.viewmodel.messages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.Message
import com.carwise.android.data.model.MessageStatus
import com.carwise.android.data.model.UserInfo
import com.carwise.android.data.model.GetListingResponse
import com.carwise.android.data.model.MessageDeliveryStatus
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.UserPayload
import com.carwise.android.data.remote.WebSocketService
import com.carwise.android.model.repository.CarwiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MessagesState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null,
    val chatUser: UserInfo? = null,
    val listing: GetListingResponse? = null,
    val isSending: Boolean = false,
    val currentUserId: String? = null,
    val isLoadingListing: Boolean = false,
    val connectionState: WebSocketService.ConnectionState = WebSocketService.ConnectionState.Disconnected,
    val messageStatuses: Map<String, MessageStatus> = emptyMap()
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repository: CarwiseRepository,
    private val webSocketService: WebSocketService
) : ViewModel() {

    private val _messagesState = MutableStateFlow(MessagesState())
    val messagesState: StateFlow<MessagesState> = _messagesState.asStateFlow()

    private var currentListingId: String? = null
    private var currentChatUserId: String? = null
    private var currentPage = 1
    private val pageSize = 50

    init {
        // Kullanıcı bilgilerini al
        repository.getCurrentUser()?.let { userPayload ->
            _messagesState.update {
                it.copy(currentUserId = userPayload.user_id)
            }
        }

        // WebSocket mesajlarını dinle
        viewModelScope.launch {
            webSocketService.messageFlow.collect { message ->
                handleIncomingMessage(message)
            }
        }

        // Bağlantı durumunu dinle
        viewModelScope.launch {
            webSocketService.connectionState.collect { state ->
                _messagesState.update { it.copy(connectionState = state) }

                if (state is WebSocketService.ConnectionState.Error) {
                    _messagesState.update { it.copy(error = state.message) }
                }
            }
        }

        // Mesaj durumu güncellemelerini dinle
        viewModelScope.launch {
            webSocketService.messageStatusFlow.collect { status ->
                handleMessageStatus(status)
            }
        }
    }

    private fun handleIncomingMessage(message: Message) {
        // Null kontrolleri
        if (message.sender == null || message.receiver == null) {
            Log.e("MessagesViewModel", "Invalid message received: sender or receiver is null")
            return
        }

        val currentState = _messagesState.value
        val currentUserId = currentState.currentUserId

        // Geçerli kullanıcı ID'si kontrolü
        if (currentUserId == null) {
            Log.e("MessagesViewModel", "Current user ID is null")
            return
        }

        // Mesajın bu sohbete ait olup olmadığını kontrol et
        val isMessageForCurrentChat = (message.sender.id == currentChatUserId || message.receiver.id == currentChatUserId)

        if (!isMessageForCurrentChat) {
            Log.d("MessagesViewModel", "Message not for current chat: ${message.id}")
            return
        }

        try {
            // Kullanıcı bilgilerini zenginleştir
            val enrichedMessage = message.copy(
                sender = if (message.sender.id == currentUserId) {
                    // Gönderen mevcut kullanıcı ise, repository'den alınan bilgileri kullan
                    repository.getCurrentUser()?.let { userPayload ->
                        UserInfo(
                            id = userPayload.user_id,
                            firstName = userPayload.first_name,
                            lastName = userPayload.last_name,
                            email = userPayload.email,
                            countryCode = userPayload.country_code,
                            phoneNumber = userPayload.phone_number
                        )
                    } ?: message.sender
                } else {
                    // Gönderen karşı taraf ise, chatUser bilgilerini kullan
                    currentState.chatUser ?: message.sender
                },
                receiver = if (message.receiver.id == currentUserId) {
                    // Alıcı mevcut kullanıcı ise, repository'den alınan bilgileri kullan
                    repository.getCurrentUser()?.let { userPayload ->
                        UserInfo(
                            id = userPayload.user_id,
                            firstName = userPayload.first_name,
                            lastName = userPayload.last_name,
                            email = userPayload.email,
                            countryCode = userPayload.country_code,
                            phoneNumber = userPayload.phone_number
                        )
                    } ?: message.receiver
                } else {
                    // Alıcı karşı taraf ise, chatUser bilgilerini kullan
                    currentState.chatUser ?: message.receiver
                }
            )

            _messagesState.update { state ->
                val messageExists = state.messages.any { it.id == enrichedMessage.id }

                if (!messageExists) {
                    // Geçici mesajları kaldır ve gerçek mesajı ekle
                    val filteredMessages = state.messages.filterNot {
                        it.id.startsWith("temp_") &&
                        it.message == enrichedMessage.message &&
                        it.sender.id == enrichedMessage.sender.id &&
                        it.receiver.id == enrichedMessage.receiver.id
                    }

                    state.copy(
                        messages = listOf(enrichedMessage) + filteredMessages,
                        messageStatuses = state.messageStatuses - enrichedMessage.id
                    )
                } else {
                    state
                }
            }
        } catch (e: Exception) {
            Log.e("MessagesViewModel", "Error handling incoming message: ${message.id}", e)
            _messagesState.update { 
                it.copy(error = "Mesaj işlenirken bir hata oluştu")
            }
        }
    }

    private fun handleMessageStatus(status: MessageStatus) {
        _messagesState.update { state ->
            state.copy(
                messageStatuses = state.messageStatuses + (status.messageId to status)
            )
        }

        // Hata durumunda error mesajını göster
        if (status.status == MessageDeliveryStatus.ERROR) {
            _messagesState.update {
                it.copy(error = status.error ?: "Mesaj gönderilirken bir hata oluştu")
            }
        }
    }

    fun initialize(listingId: String, userId: String) {
        if (currentListingId == listingId && currentChatUserId == userId) {
            return
        }

        currentListingId = listingId
        currentChatUserId = userId

        // WebSocket bağlantısını kur
        webSocketService.connect(listingId, userId)

        // İlk verileri yükle
        loadInitialData(listingId, userId)
    }

    private fun loadInitialData(listingId: String, userId: String) {
        // Kullanıcı bilgilerini yükle
        loadUserInfo(userId)

        // İlan detaylarını yükle
        loadListingDetails(listingId)

        // Mesajları yükle
        loadMessages(listingId, userId)
    }

    private fun loadUserInfo(userId: String) {
        if (_messagesState.value.chatUser?.id == userId) {
            return
        }

        viewModelScope.launch {
            try {
                when (val result = repository.getUserInfo(userId)) {
                    is ResultState.Success -> {
                        _messagesState.update {
                            it.copy(chatUser = result.data)
                        }
                    }
                    is ResultState.Error -> {
                        _messagesState.update {
                            it.copy(error = result.error.error ?: "Kullanıcı bilgileri alınamadı")
                        }
                    }
                    is ResultState.Loading -> {
                        // Loading state'inde bir şey yapma
                    }
                }
            } catch (e: Exception) {
                _messagesState.update {
                    it.copy(error = e.message ?: "Kullanıcı bilgileri alınamadı")
                }
            }
        }
    }

    private fun loadListingDetails(listingId: String) {
        viewModelScope.launch {
            _messagesState.update { it.copy(isLoadingListing = true) }
            try {
                when (val result = repository.getListingDetail(listingId)) {
                    is ResultState.Success -> {
                        _messagesState.update {
                            it.copy(
                                listing = result.data,
                                isLoadingListing = false,
                                error = null
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _messagesState.update {
                            it.copy(
                                isLoadingListing = false,
                                error = result.error.error ?: "İlan detayları alınamadı"
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _messagesState.update { it.copy(isLoadingListing = true) }
                    }
                }
            } catch (e: Exception) {
                _messagesState.update {
                    it.copy(
                        isLoadingListing = false,
                        error = e.message ?: "İlan detayları alınamadı"
                    )
                }
            }
        }
    }

    fun loadMessages(listingId: String, userId: String) {
        // Aynı konuşma için mesajlar zaten yüklenmişse tekrar yükleme
        if (listingId == currentListingId && userId == currentChatUserId &&
            _messagesState.value.messages.isNotEmpty() && !_messagesState.value.isLoading) {
            return
        }

        currentListingId = listingId
        currentChatUserId = userId
        currentPage = 1

        viewModelScope.launch {
            _messagesState.update { it.copy(isLoading = true, error = null, endReached = false) }
            try {
                when (val result = repository.getMessages(listingId, userId, currentPage, pageSize)) {
                    is ResultState.Success -> {
                        // Chat kullanıcısını ilk mesajdan al
                        val chatUser = result.data.messages.firstOrNull()?.let { message ->
                            if (message.sender.id == _messagesState.value.currentUserId) {
                                message.receiver
                            } else {
                                message.sender
                            }
                        }

                        _messagesState.update {
                            it.copy(
                                messages = result.data.messages,
                                chatUser = chatUser ?: it.chatUser,
                                isLoading = false,
                                endReached = result.data.messages.size < pageSize,
                                error = null
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _messagesState.update {
                            it.copy(
                                isLoading = false,
                                error = result.error.error ?: "Mesajlar yüklenirken bir hata oluştu"
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _messagesState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _messagesState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Mesajlar yüklenirken bir hata oluştu"
                    )
                }
            }
        }
    }

    fun loadMoreMessages(listingId: String, userId: String) {
        val currentState = _messagesState.value

        if (currentState.isPaginating ||
            currentState.endReached ||
            currentState.messages.isEmpty() ||
            listingId != currentListingId ||
            userId != currentChatUserId) {
            return
        }

        viewModelScope.launch {
            _messagesState.update { it.copy(isPaginating = true, error = null) }
            try {
                val nextPage = currentPage + 1

                when (val result = repository.getMessages(listingId, userId, nextPage, pageSize)) {
                    is ResultState.Success -> {
                        val newMessages = result.data.messages
                        if (newMessages.isNotEmpty()) {
                            _messagesState.update {
                                it.copy(
                                    messages = it.messages + newMessages,
                                    isPaginating = false,
                                    endReached = newMessages.size < pageSize,
                                    error = null
                                )
                            }
                            currentPage = nextPage
                        } else {
                            _messagesState.update {
                                it.copy(
                                    isPaginating = false,
                                    endReached = true
                                )
                            }
                        }
                    }
                    is ResultState.Error -> {
                        _messagesState.update {
                            it.copy(
                                isPaginating = false,
                                error = result.error.error ?: "Daha fazla mesaj yüklenirken bir hata oluştu"
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _messagesState.update { it.copy(isPaginating = true) }
                    }
                }
            } catch (e: Exception) {
                _messagesState.update {
                    it.copy(
                        isPaginating = false,
                        error = e.message ?: "Daha fazla mesaj yüklenirken bir hata oluştu"
                    )
                }
            }
        }
    }

    fun sendMessage(listingId: String, userId: String, content: String) {
        if (content.isBlank()) return

        if (listingId != currentListingId || userId != currentChatUserId) {
            _messagesState.update {
                it.copy(error = "Mesaj gönderilemiyor: Geçersiz konuşma")
            }
            return
        }

        if (!webSocketService.isConnected()) {
            _messagesState.update {
                it.copy(error = "Mesaj gönderilemiyor: Bağlantı yok")
            }
            return
        }

        val currentUser = repository.getCurrentUser()
        if (currentUser == null) {
            _messagesState.update {
                it.copy(error = "Kullanıcı bilgileri alınamadı")
            }
            return
        }

        val chatUser = _messagesState.value.chatUser
        if (chatUser == null) {
            _messagesState.update {
                it.copy(error = "Alıcı bilgileri bulunamadı")
            }
            return
        }

        // Geçici ID oluştur
        val tempId = "temp_${UUID.randomUUID()}"
        val currentTimestamp = System.currentTimeMillis() / 1000 // Unix timestamp in seconds

        val message = Message(
            id = tempId,
            sender = UserInfo(
                id = currentUser.user_id,
                firstName = currentUser.first_name,
                lastName = currentUser.last_name,
                email = currentUser.email,
                countryCode = currentUser.country_code,
                phoneNumber = currentUser.phone_number,
            ),
            receiver = chatUser,
            message = content.trim(),
            read = false,
            createdAt = currentTimestamp
        )

        // Mesajı HEMEN yerel state'e ekle - UI güncellemesi için
        _messagesState.update { currentState ->
            currentState.copy(
                messages = listOf(message) + currentState.messages,
                messageStatuses = currentState.messageStatuses + (message.id to MessageStatus(
                    messageId = message.id,
                    status = MessageDeliveryStatus.SENDING,
                )),
                isSending = false // UI'da loading gösterme, zaten geçici mesaj var
            )
        }

        // Coroutine ile WebSocket gönderimi yap (UI'yı bloklamaz)
        viewModelScope.launch {
            try {
                // WebSocket üzerinden gönder
                webSocketService.sendMessage(message, listingId)
            } catch (e: Exception) {
                // Hata durumunda geçici mesajı kaldır
                _messagesState.update { currentState ->
                    currentState.copy(
                        messages = currentState.messages.filterNot { it.id == tempId },
                        messageStatuses = currentState.messageStatuses - tempId,
                        error = "Mesaj gönderilemedi: ${e.message}"
                    )
                }
            }
        }
    }

    fun reconnect() {
        currentListingId?.let { listingId ->
            currentChatUserId?.let { userId ->
                webSocketService.connect(listingId, userId)
            }
        }
    }

    fun refreshMessages(listingId: String, userId: String) {
        // Mevcut mesajları temizle ve yeniden yükle
        _messagesState.update {
            it.copy(
                messages = emptyList(),
                messageStatuses = emptyMap(),
                endReached = false
            )
        }

        currentPage = 1
        loadMessages(listingId, userId)
        loadListingDetails(listingId)
    }

    fun clearError() {
        _messagesState.update { it.copy(error = null) }
    }

    fun isMessageFromCurrentUser(message: Message): Boolean {
        return message.sender.id == _messagesState.value.currentUserId
    }

    fun getMessageStatus(messageId: String): MessageStatus? {
        return _messagesState.value.messageStatuses[messageId]
    }

    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
}