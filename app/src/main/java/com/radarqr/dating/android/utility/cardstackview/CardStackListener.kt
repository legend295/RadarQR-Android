package com.radarqr.dating.android.utility.cardstackview

import android.view.View

interface CardStackListener {
    fun onCardDragging(direction: Direction?, ratio: Float)
    fun onCardSwiped(direction: Direction?)
    fun onCardRewound(position: Int)
    fun onCardCanceled()
    fun onCardAppeared(view: View?, position: Int)
    fun onCardDisappeared(view: View?, position: Int)

    companion object{
        fun default() = object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {}
            override fun onCardSwiped(direction: Direction?) {}
            override fun onCardRewound(position: Int) {}
            override fun onCardCanceled() {}
            override fun onCardAppeared(view: View?, position: Int) {}
            override fun onCardDisappeared(view: View?, position: Int) {}
        }
    }
}