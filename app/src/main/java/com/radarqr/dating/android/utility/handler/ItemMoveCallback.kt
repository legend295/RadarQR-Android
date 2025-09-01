package com.radarqr.dating.android.utility.handler

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.ui.welcome.registerScreens.adapter.ImageStringAdapter


class ItemMoveCallback(private val adapter: ItemTouchHelperContract) : ItemTouchHelper.Callback() {

    /*  override fun isLongPressDragEnabled(): Boolean {
          return true
      }


  */
    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags =
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(
            dragFlags,
            0
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // Notify the adapter of the move
        return adapter.onRowMoved(source.absoluteAdapterPosition, target.absoluteAdapterPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    interface ItemTouchHelperContract {
        fun onRowMoved(fromPosition: Int, toPosition: Int): Boolean
        fun onRowSelected(myViewHolder: ImageStringAdapter.ViewHolder?)
        fun onRowClear(myViewHolder: ImageStringAdapter.ViewHolder?)
    }
}