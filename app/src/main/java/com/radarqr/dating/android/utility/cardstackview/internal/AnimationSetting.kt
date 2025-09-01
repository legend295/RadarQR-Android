package com.radarqr.dating.android.utility.cardstackview.internal

import android.view.animation.Interpolator
import com.radarqr.dating.android.utility.cardstackview.Direction

interface AnimationSetting {
    fun getDirection(): Direction
    fun getDuration(): Int
    fun getInterpolator(): Interpolator
}