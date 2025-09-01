package com.radarqr.dating.android.utility.cardstackview.internal

import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.utility.cardstackview.CardStackLayoutManager
import kotlin.math.min

class CardStackDataObserver(private val recyclerView: RecyclerView) :
    RecyclerView.AdapterDataObserver() {
    override fun onChanged() {
        val manager: CardStackLayoutManager = cardStackLayoutManager
        manager.setTopPosition(0)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        // Do nothing
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        // Do nothing
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        // Do nothing
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        val manager: CardStackLayoutManager = cardStackLayoutManager
        val topPosition: Int = manager.getTopPosition()
        if (manager.itemCount == 0) {
            manager.setTopPosition(0)
        } else if (positionStart < topPosition) {

            val diff = topPosition - positionStart
            manager.setTopPosition(min(topPosition - diff, manager.itemCount - 1))
        }
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        val manager: CardStackLayoutManager = cardStackLayoutManager
        manager.removeAllViews()
    }

    private val cardStackLayoutManager: CardStackLayoutManager
        get() {
            val manager = recyclerView.layoutManager
            if (manager is CardStackLayoutManager) {
                return manager
            }
            throw IllegalStateException("CardStackView must be set CardStackLayoutManager.")
        }

}