package com.radarqr.dating.android.utility.cardstackview.internal

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.radarqr.dating.android.utility.cardstackview.*

class CardStackSetting {
    var stackFrom: StackFrom = StackFrom.Bottom
    var visibleCount = 2
    var translationInterval = 8.0f
    var scaleInterval = 0.95f // 0.0f - 1.0f

    var swipeThreshold = 0.3f // 0.0f - 1.0f

    var maxDegree = 20.0f
    var directions: List<Direction> = Direction.HORIZONTAL
    var canScrollHorizontal = true
    var canScrollVertical = true
    var swipeableMethod: SwipeableMethod = SwipeableMethod.Automatic
    var swipeAnimationSetting: SwipeAnimationSetting =
        SwipeAnimationSetting.Companion.Builder().build()
    var rewindAnimationSetting: RewindAnimationSetting =
        RewindAnimationSetting.Companion.Builder().build()
    var overlayInterpolator: Interpolator = LinearInterpolator()
}