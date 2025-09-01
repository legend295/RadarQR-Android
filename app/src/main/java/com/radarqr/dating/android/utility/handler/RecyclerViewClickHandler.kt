package com.radarqr.dating.android.utility.handler

interface RecyclerViewClickHandler<K, L, M> {
    fun onClick(k: K, l: L, m: M)
    fun onLongClick(k: K, l: L, m: M)
}