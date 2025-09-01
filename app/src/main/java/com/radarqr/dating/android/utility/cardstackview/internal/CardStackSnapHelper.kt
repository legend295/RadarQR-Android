package com.radarqr.dating.android.utility.cardstackview.internal

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.radarqr.dating.android.utility.cardstackview.CardStackLayoutManager
import com.radarqr.dating.android.utility.cardstackview.Duration
import com.radarqr.dating.android.utility.cardstackview.SwipeAnimationSetting
import kotlin.math.abs

class CardStackSnapHelper : SnapHelper() {
    private var velocityX = 0
    private var velocityY = 0

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        if (layoutManager is CardStackLayoutManager) {
            val manager: CardStackLayoutManager = layoutManager
            if (manager.findViewByPosition(manager.getTopPosition()) != null) {
                val x = targetView.translationX.toInt()
                val y = targetView.translationY.toInt()
                if (x != 0 || y != 0) {
                    val setting: CardStackSetting = manager.getCardStackSetting()
                    val horizontal = abs(x) / targetView.width.toFloat()
                    val vertical = abs(y) / targetView.height.toFloat()
                    val duration: Duration =
                        Duration.fromVelocity(if (velocityY < velocityX) velocityX else velocityY)
                    if (duration == Duration.Fast || setting.swipeThreshold < horizontal || setting.swipeThreshold < vertical) {
                        val state: CardStackState = manager.getCardStackState()
                        if (setting.directions.contains(state.getDirection())) {
                            state.targetPosition = state.topPosition + 1
                            val swipeAnimationSetting: SwipeAnimationSetting =
                                SwipeAnimationSetting.Companion.Builder()
                                    .setDirection(setting.swipeAnimationSetting.getDirection())
                                    .setDuration(duration.duration)
                                    .setInterpolator(setting.swipeAnimationSetting.getInterpolator())
                                    .build()
                            manager.setSwipeAnimationSetting(swipeAnimationSetting)
                            velocityX = 0
                            velocityY = 0
                            val scroller = CardStackSmoothScroller(
                                CardStackSmoothScroller.ScrollType.ManualSwipe,
                                manager
                            )
                            scroller.targetPosition = manager.getTopPosition()
                            manager.startSmoothScroll(scroller)
                        } else {
                            val scroller = CardStackSmoothScroller(
                                CardStackSmoothScroller.ScrollType.ManualCancel,
                                manager
                            )
                            scroller.targetPosition = manager.getTopPosition()
                            manager.startSmoothScroll(scroller)
                        }
                    } else {
                        val scroller = CardStackSmoothScroller(
                            CardStackSmoothScroller.ScrollType.ManualCancel,
                            manager
                        )
                        scroller.targetPosition = manager.getTopPosition()
                        manager.startSmoothScroll(scroller)
                    }
                }
            }
        }
        return IntArray(2)
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager is CardStackLayoutManager) {
            val manager: CardStackLayoutManager = layoutManager
            val view: View? = manager.findViewByPosition(manager.getTopPosition())
            if (view != null) {
                val x = view.translationX.toInt()
                val y = view.translationY.toInt()
                return if (x == 0 && y == 0) {
                    null
                } else view
            }
        }
        return null
    }

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        this.velocityX = abs(velocityX)
        this.velocityY = abs(velocityY)
        if (layoutManager is CardStackLayoutManager) {
            val manager: CardStackLayoutManager = layoutManager
            return manager.getTopPosition()
        }
        return RecyclerView.NO_POSITION
    }
}
