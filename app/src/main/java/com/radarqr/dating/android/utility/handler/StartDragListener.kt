package com.radarqr.dating.android.utility.handler

import androidx.recyclerview.widget.RecyclerView

interface StartDragListener {
    fun requestDrag(viewHolder: RecyclerView.ViewHolder?)
}