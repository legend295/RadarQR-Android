package com.radarqr.dating.android.utility

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class RecyclerViewLoadMoreScroll(var layoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val ItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val ItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (!isLoading) {
            if (ItemCount + ItemPosition >= totalItemCount
                && ItemPosition >= 0) {
                loadMore()
            }
        }
    }

    protected abstract fun loadMore()
    abstract val isLoading: Boolean

}