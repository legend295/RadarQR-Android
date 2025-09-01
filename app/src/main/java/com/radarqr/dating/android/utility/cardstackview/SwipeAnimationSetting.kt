package com.radarqr.dating.android.utility.cardstackview

import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator
import com.radarqr.dating.android.utility.cardstackview.internal.AnimationSetting

class SwipeAnimationSetting(
    private var direction: Direction,
    private var duration: Int,
    private var interpolator: Interpolator
) : AnimationSetting {
    override fun getDirection(): Direction = direction

    override fun getDuration(): Int = duration

    override fun getInterpolator(): Interpolator = interpolator

    companion object {
        class Builder {
            private var direction = Direction.Right
            private var duration = Duration.Normal.duration
            private var interpolator: Interpolator = AccelerateInterpolator()

            fun setDirection(direction: Direction): Builder {
                this.direction = direction
                return this
            }

            fun setDuration(duration: Int): Builder {
                this.duration = duration
                return this
            }

            fun setInterpolator(interpolator: Interpolator): Builder {
                this.interpolator = interpolator
                return this
            }

            fun build(): SwipeAnimationSetting {
                return SwipeAnimationSetting(
                    direction,
                    duration,
                    interpolator
                )
            }
        }
    }
}