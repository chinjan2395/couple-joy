package com.chinjan.couplejoy.data.model

import com.google.firebase.Timestamp

data class Message(
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val device: String = "",
    val uid: String = "",
)