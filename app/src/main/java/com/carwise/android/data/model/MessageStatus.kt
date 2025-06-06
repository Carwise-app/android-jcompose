package com.carwise.android.data.model

enum class MessageDeliveryStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    ERROR
}

data class MessageStatus(
    val messageId: String,
    val status: MessageDeliveryStatus,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val maxRetries: Int = 3
) {
    fun canRetry(): Boolean = retryCount < maxRetries && status == MessageDeliveryStatus.ERROR
    
    fun incrementRetry(): MessageStatus = copy(
        retryCount = retryCount + 1,
        timestamp = System.currentTimeMillis()
    )
} 