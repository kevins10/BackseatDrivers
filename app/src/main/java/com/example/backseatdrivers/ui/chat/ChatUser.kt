package com.example.backseatdrivers.ui.chat

import java.io.Serializable

data class ChatUser(
    var userName: String? = "",
    var userEmail: String? = "",
    var userID: String? = ""
) : Serializable
