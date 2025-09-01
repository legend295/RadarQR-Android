package com.radarqr.dating.android.ui.home.quickBlox.listeners

import android.view.View
import com.quickblox.chat.model.QBChatMessage


interface MessageLongClickListener {
    fun onMessageLongClicked(itemViewType: Int?, view: View, qbChatMessage: QBChatMessage?)
}