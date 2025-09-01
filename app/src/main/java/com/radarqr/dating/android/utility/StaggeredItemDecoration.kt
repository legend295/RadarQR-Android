package com.radarqr.dating.android.utility

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class StaggeredItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = space / 3
        outRect.right = space / 3
        outRect.bottom = space / 2

        if (parent.getChildAdapterPosition(view) == 1) {
            outRect.top = space
        }
    }


}