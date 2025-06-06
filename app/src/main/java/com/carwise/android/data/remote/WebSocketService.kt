package com.carwise.android.data.remote

import android.util.Log
import com.carwise.android.data.model.Message
import com.carwise.android.data.model.MessageDeliveryStatus
import com.carwise.android.data.model.MessageStatus
import com.carwise.android.data.model.UserInfo
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import java.util.UUID

@Singleton
class WebSocketService @Inject constructor(
    private val authStorage: com.carwise.android.data.local.AuthStorage.AuthStorage
) {
    private var webSocket: WebSocket? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private var currentListingId: String? = null
    private var currentReceiverId: String? = null

    // CoroutineScope with SupervisorJob for better error handling
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val _messageFlow = MutableSharedFlow<Message>(replay = 0, extraBufferCapacity = 10)
    val messageFlow: SharedFlow<Message> = _messageFlow

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _messageStatusFlow = MutableSharedFlow<MessageStatus>(replay = 0, extraBufferCapacity = 10)
    val messageStatusFlow: SharedFlow<MessageStatus> = _messageStatusFlow

    sealed class ConnectionState {
        object Connected : ConnectionState()
        object Connecting : ConnectionState()
        object Disconnected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    fun connect(listingId: String, receiverId: String) {
        if (_connectionState.value is ConnectionState.Connected &&
            listingId == currentListingId &&
            receiverId == currentReceiverId) {
            return
        }

        // Önce mevcut bağlantıyı kapat
        disconnect()

        currentListingId = listingId
        currentReceiverId = receiverId
        reconnectAttempts = 0
        establishConnection(listingId, receiverId)
    }

    private fun establishConnection(listingId: String, receiverId: String) {
        val token = authStorage.getAccessToken()
        if (token.isNullOrEmpty()) {
            _connectionState.value = ConnectionState.Error("Token bulunamadı")
            return
        }

        val url = "wss://carwisegw.yusuftalhaklc.com/chat/ws/$listingId/$receiverId?token=$token"

        _connectionState.value = ConnectionState.Connecting

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connection opened")
                reconnectAttempts = 0
                _connectionState.value = ConnectionState.Connected
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text")
                try {
                    when {
                        text.startsWith("status:") -> {
                            val statusJson = text.substring(7)
                            try {
                                val status = gson.fromJson(statusJson, MessageStatus::class.java)
                                if (status.messageId.isBlank()) {
                                    Log.e("WebSocket", "Invalid status message: messageId is empty")
                                    return
                                }
                                serviceScope.launch {
                                    _messageStatusFlow.emit(status)
                                }
                            } catch (e: Exception) {
                                Log.e("WebSocket", "Error parsing status message: $statusJson", e)
                            }
                        }
                        else -> {
                            try {
                                val wsMessage = gson.fromJson(text, WebSocketMessageResponse::class.java)
                                
                                // Mesaj validasyonu
                                if (wsMessage.message.isBlank()) {
                                    Log.e("WebSocket", "Invalid message: message content is empty")
                                    return
                                }
                                if (wsMessage.sender_id.isBlank()) {
                                    Log.e("WebSocket", "Invalid message: sender_id is empty")
                                    return
                                }
                                if (wsMessage.receiver_id.isBlank()) {
                                    Log.e("WebSocket", "Invalid message: receiver_id is empty")
                                    return
                                }
                                if (wsMessage.listing_id.isBlank()) {
                                    Log.e("WebSocket", "Invalid message: listing_id is empty")
                                    return
                                }

                                // WebSocket mesajını Message modeline dönüştür
                                val message = Message(
                                    id = wsMessage.id ?: UUID.randomUUID().toString(),
                                    sender = UserInfo(
                                        id = wsMessage.sender_id,
                                        firstName = "", // Bu bilgileri daha sonra doldur
                                        lastName = "",
                                        email = "",
                                        countryCode = "",
                                        phoneNumber = ""
                                    ),
                                    receiver = UserInfo(
                                        id = wsMessage.receiver_id,
                                        firstName = "", // Bu bilgileri daha sonra doldur
                                        lastName = "",
                                        email = "",
                                        countryCode = "",
                                        phoneNumber = ""
                                    ),
                                    message = wsMessage.message,
                                    read = false,
                                    createdAt = wsMessage.timestamp
                                )

                                serviceScope.launch {
                                    _messageFlow.emit(message)
                                }
                            } catch (e: Exception) {
                                Log.e("WebSocket", "Error parsing message: $text", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error processing message", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Connection failed", t)
                _connectionState.value = ConnectionState.Error(t.message ?: "Bağlantı hatası")

                // Exponential backoff ile yeniden bağlanma
                if (reconnectAttempts < maxReconnectAttempts) {
                    reconnectAttempts++
                    val delay = (1000L * (2.0.pow(reconnectAttempts.toDouble()))).toLong()
                    serviceScope.launch {
                        kotlinx.coroutines.delay(delay.coerceAtMost(30000))
                        if (currentListingId == listingId && currentReceiverId == receiverId) {
                            establishConnection(listingId, receiverId)
                        }
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Connection closed: $reason")
                _connectionState.value = ConnectionState.Disconnected
            }
        })
    }

    data class WebSocketMessageRequest(
        val message: String,
        val sender_id: String,
        val receiver_id: String,
        val listing_id: String,
        val timestamp: Long
    )

    data class WebSocketMessageResponse(
        val message: String,
        val sender_id: String,
        val receiver_id: String,
        val listing_id: String,
        val timestamp: Long,
        val id: String? = null
    )

    fun sendMessage(message: Message, listingId: String) {
        if (_connectionState.value !is ConnectionState.Connected) {
            Log.e("WebSocket", "Cannot send message: WebSocket is not connected")
            serviceScope.launch {
                _messageStatusFlow.emit(
                    MessageStatus(
                        messageId = message.id,
                        status = MessageDeliveryStatus.ERROR,
                        error = "Bağlantı yok"
                    )
                )
            }
            return
        }

        try {
            // WebSocket için doğru format
            val webSocketMessage = WebSocketMessageRequest(
                message = message.message,
                sender_id = message.sender.id,
                receiver_id = message.receiver.id,
                listing_id = listingId,
                timestamp = message.createdAt
            )

            val messageJson = gson.toJson(webSocketMessage)
            val success = webSocket?.send(messageJson) ?: false

            if (!success) {
                Log.e("WebSocket", "Failed to send message")
                serviceScope.launch {
                    _messageStatusFlow.emit(
                        MessageStatus(
                            messageId = message.id,
                            status = MessageDeliveryStatus.ERROR,
                            error = "Mesaj gönderilemedi"
                        )
                    )
                }
            } else {
                Log.d("WebSocket", "Message sent successfully: $messageJson")
                serviceScope.launch {
                    _messageStatusFlow.emit(
                        MessageStatus(
                            messageId = message.id,
                            status = MessageDeliveryStatus.SENT
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Error sending message", e)
            serviceScope.launch {
                _messageStatusFlow.emit(
                    MessageStatus(
                        messageId = message.id,
                        status = MessageDeliveryStatus.ERROR,
                        error = e.message ?: "Mesaj gönderilirken hata oluştu"
                    )
                )
            }
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User disconnected")
        } catch (e: Exception) {
            Log.e("WebSocket", "Error closing websocket", e)
        } finally {
            webSocket = null
            currentListingId = null
            currentReceiverId = null
            reconnectAttempts = 0
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    fun isConnected(): Boolean {
        return _connectionState.value is ConnectionState.Connected
    }

    fun getCurrentConnection(): Pair<String?, String?> {
        return Pair(currentListingId, currentReceiverId)
    }
}