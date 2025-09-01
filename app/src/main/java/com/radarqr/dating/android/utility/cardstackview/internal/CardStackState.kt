package com.radarqr.dating.android.utility.cardstackview.internal

import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.utility.cardstackview.Direction
import kotlin.math.abs
import kotlin.math.min

class CardStackState {

    var status: Status = Status.Idle
    var width = 0
    var height = 0
    var dx = 0
    var dy = 0
    var topPosition = 0
    var targetPosition = RecyclerView.NO_POSITION
    var proportion = 0.0f

    enum class Status {
        Idle,
        Dragging,
        RewindAnimating,
        AutomaticSwipeAnimating,
        AutomaticSwipeAnimated,
        ManualSwipeAnimating,
        ManualSwipeAnimated;

        fun isBusy(): Boolean {
            return this != Idle
        }

        fun isDragging(): Boolean {
            return this == Dragging
        }

        fun isSwipeAnimating(): Boolean {
            return this == ManualSwipeAnimating || this == AutomaticSwipeAnimating
        }

        fun toAnimatedStatus(): Status {
            return when (this) {
                ManualSwipeAnimating -> ManualSwipeAnimated
                AutomaticSwipeAnimating -> AutomaticSwipeAnimated
                else -> Idle
            }
        }
    }

    fun next(state: Status) {
        status = state
    }

    fun getDirection(): Direction {
        return if (abs(dy) < abs(dx)) {
            if (dx < 0.0f) {
                Direction.Left
            } else {
                Direction.Right
            }
        } else {
            if (dy < 0.0f) {
                Direction.Top
            } else {
                Direction.Bottom
            }
        }
    }

    fun getRatio(): Float {
        val absDx = abs(dx)
        val absDy = abs(dy)
        val ratio: Float = if (absDx < absDy) {
            absDy / (height / 2.0f)
        } else {
            absDx / (width / 2.0f)
        }
        return min(ratio, 1.0f)
    }

    fun isSwipeCompleted(): Boolean {
        if (status.isSwipeAnimating()) {
            if (topPosition < targetPosition) {
                if (width < abs(dx) || height < abs(dy)) {
                    return true
                }
            }
        }
        return false
    }

    fun canScrollToPosition(position: Int, itemCount: Int): Boolean {
        if (position == topPosition) {
            return false
        }
        if (position < 0) {
            return false
        }
        if (itemCount < position) {
            return false
        }
        return !status.isBusy()
    }
}