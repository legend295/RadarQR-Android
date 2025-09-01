package com.radarqr.dating.android.utility.ballScaleAnimation

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.view.animation.LinearInterpolator

class BallScaleIndicator : Indicator() {

    var scale = 1f
    var localAlpha = 255

    override fun draw(canvas: Canvas, paint: Paint) {
        val circleSpacing = 4f
        paint.alpha = localAlpha
        canvas.scale(scale, scale, (getWidth() / 2).toFloat(), (getHeight() / 2).toFloat())
        paint.alpha = localAlpha
        canvas.drawCircle(
            (getWidth() / 2).toFloat(),
            (getHeight() / 2).toFloat(), getWidth() / 2 - circleSpacing, paint
        )
    }

    override fun onCreateAnimators(): ArrayList<ValueAnimator> {
        val animators: ArrayList<ValueAnimator> = ArrayList()
        val scaleAnim = ValueAnimator.ofFloat(0f, 1f)
        scaleAnim.interpolator = LinearInterpolator()
        scaleAnim.duration = 1000
        scaleAnim.repeatCount = -1
        addUpdateListener(
            scaleAnim
        ) { animation ->
            scale = animation.animatedValue as Float
            postInvalidate()
        }

        val alphaAnim = ValueAnimator.ofInt(255, 0)
        alphaAnim.interpolator = LinearInterpolator()
        alphaAnim.duration = 1000
        alphaAnim.repeatCount = -1
        addUpdateListener(
            alphaAnim
        ) { animation ->
            localAlpha = (animation.animatedValue as Int)
            postInvalidate()
        }
        animators.add(scaleAnim)
        animators.add(alphaAnim)
        return animators
    }
}