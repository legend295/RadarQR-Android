package com.radarqr.dating.android.utility.cardstackview.internal

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.utility.cardstackview.CardStackLayoutManager
import com.radarqr.dating.android.utility.cardstackview.CardStackListener
import com.radarqr.dating.android.utility.cardstackview.Direction
import com.radarqr.dating.android.utility.cardstackview.RewindAnimationSetting

class CardStackSmoothScroller(val type:ScrollType, val manager: CardStackLayoutManager): RecyclerView.SmoothScroller() {

    enum class ScrollType {
        AutomaticSwipe, AutomaticRewind, ManualSwipe, ManualCancel
    }

    override fun onStart() {
        val listener = manager.getCardStackListener()
        val state = manager.getCardStackState()
        when (type) {
            ScrollType.AutomaticSwipe -> {
                state.next(CardStackState.Status.AutomaticSwipeAnimating)
                listener.onCardDisappeared(manager.getTopView(), manager.getTopPosition())
            }
            ScrollType.AutomaticRewind -> state.next(CardStackState.Status.RewindAnimating)
            ScrollType.ManualSwipe -> {
                state.next(CardStackState.Status.ManualSwipeAnimating)
                listener.onCardDisappeared(manager.getTopView(), manager.getTopPosition())
            }
            ScrollType.ManualCancel -> state.next(CardStackState.Status.RewindAnimating)
        }
    }

    override fun onStop() {
        val listener: CardStackListener = manager.getCardStackListener()
        when (type) {
            ScrollType.AutomaticSwipe -> {}
            ScrollType.AutomaticRewind -> {
                listener.onCardRewound(manager.getTopPosition())
                listener.onCardAppeared(manager.getTopView(), manager.getTopPosition())
            }
            ScrollType.ManualSwipe -> {}
            ScrollType.ManualCancel -> listener.onCardCanceled()
        }
    }

    override fun onSeekTargetStep(dx: Int, dy: Int, state: RecyclerView.State, action: Action) {
        if (type == ScrollType.AutomaticRewind) {
            val setting: RewindAnimationSetting =
                manager.getCardStackSetting().rewindAnimationSetting
            action.update(
                -getDx(setting),
                -getDy(setting),
                setting.getDuration(),
                setting.getInterpolator()
            )
        }
    }

    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        val x = targetView.translationX.toInt()
        val y = targetView.translationY.toInt()
        val setting: AnimationSetting
        when (type) {
            ScrollType.AutomaticSwipe -> {
                setting = manager.getCardStackSetting().swipeAnimationSetting
                action.update(
                    -getDx(setting),
                    -getDy(setting),
                    setting.getDuration(),
                    setting.getInterpolator()
                )
            }
            ScrollType.AutomaticRewind -> {
                setting = manager.getCardStackSetting().rewindAnimationSetting
                action.update(
                    x,
                    y,
                    setting.getDuration(),
                    setting.getInterpolator()
                )
            }
            ScrollType.ManualSwipe -> {
                val dx = -x * 10
                val dy = -y * 10
                setting = manager.getCardStackSetting().swipeAnimationSetting
                action.update(
                    dx,
                    dy,
                    setting.getDuration(),
                    setting.getInterpolator()
                )
            }
            ScrollType.ManualCancel -> {
                setting = manager.getCardStackSetting().rewindAnimationSetting
                action.update(
                    x,
                    y,
                    setting.getDuration(),
                    setting.getInterpolator()
                )
            }
        }
    }

    private fun getDx(setting: AnimationSetting): Int {
        val state: CardStackState = manager.getCardStackState()
        val dx: Int = when (setting.getDirection()) {
            Direction.Left -> -state.width * 2
            Direction.Right -> state.width * 2
            Direction.Top, Direction.Bottom -> 0
        }
        return dx
    }

    private fun getDy(setting: AnimationSetting): Int {
        val state: CardStackState = manager.getCardStackState()
        val dy: Int = when (setting.getDirection()) {
            Direction.Left, Direction.Right -> state.height / 4
            Direction.Top -> -state.height * 2
            Direction.Bottom -> state.height * 2
        }
        return dy
    }
}