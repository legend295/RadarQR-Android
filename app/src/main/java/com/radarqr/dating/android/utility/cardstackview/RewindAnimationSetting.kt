package com.radarqr.dating.android.utility.cardstackview

import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import com.radarqr.dating.android.utility.cardstackview.internal.AnimationSetting

class RewindAnimationSetting(
    private var direction: Direction,
    private var duration: Int,
    private var interpolator: Interpolator
) : AnimationSetting {

    override fun getDirection(): Direction = direction

    override fun getDuration(): Int = duration

    override fun getInterpolator(): Interpolator = interpolator

    companion object {
        class Builder {
            private var direction = Direction.Bottom
            private var duration = Duration.Normal.duration
            private var interpolator: Interpolator = DecelerateInterpolator()

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

            fun build(): RewindAnimationSetting {
                return RewindAnimationSetting(
                    direction,
                    duration,
                    interpolator
                )
            }
        }
    }
}