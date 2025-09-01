package com.radarqr.dating.android.utility.ballScaleAnimation

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable

abstract class Indicator : Drawable(), Animatable {

    private val mUpdateListeners: HashMap<ValueAnimator, ValueAnimator.AnimatorUpdateListener> =
        HashMap()

    private var mAnimators: ArrayList<ValueAnimator>? = null
    private var alpha = 255
    private val ZERO_BOUNDS_RECT: Rect = Rect()
    private var drawBounds: Rect = ZERO_BOUNDS_RECT

    private var mHasAnimators = false

    private val mPaint: Paint = Paint()

    init {
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
    }

    open fun getColor(): Int {
        return mPaint.color
    }

    open fun setColor(color: Int) {
        mPaint.color = color
    }

    override fun setAlpha(alpha: Int) {
        this.alpha = alpha
    }

    override fun getAlpha(): Int {
        return alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun draw(canvas: Canvas) {
        draw(canvas, mPaint)
    }

    abstract fun draw(canvas: Canvas, paint: Paint)

    abstract fun onCreateAnimators(): ArrayList<ValueAnimator>

    override fun start() {
        ensureAnimators()
        if (mAnimators == null) {
            return
        }

        // If the animators has not ended, do nothing.
        if (isStarted()) {
            return
        }
        startAnimators()
        invalidateSelf()
    }

    private fun startAnimators() {
        for (i in mAnimators!!.indices) {
            val animator = mAnimators!![i]

            //when the animator restart , add the updateListener again because they
            // was removed by animator stop .
            val updateListener = mUpdateListeners[animator]
            if (updateListener != null) {
                animator.addUpdateListener(updateListener)
            }
            animator.start()
        }
    }

    private fun stopAnimators() {
        if (mAnimators != null) {
            for (animator in mAnimators!!) {
                if (animator.isStarted) {
                    animator.removeAllUpdateListeners()
                    animator.end()
                }
            }
        }
    }

    private fun ensureAnimators() {
        if (!mHasAnimators) {
            mAnimators = onCreateAnimators()
            mHasAnimators = true
        }
    }

    override fun stop() {
        stopAnimators()
    }

    private fun isStarted(): Boolean {
        for (animator in mAnimators!!) {
            return animator.isStarted
        }
        return false
    }

    override fun isRunning(): Boolean {
        for (animator in mAnimators!!) {
            return animator.isRunning
        }
        return false
    }

    /**
     * Your should use this to add AnimatorUpdateListener when
     * create animator , otherwise , animator doesn't work when
     * the animation restart .
     * @param updateListener
     */
    open fun addUpdateListener(
        animator: ValueAnimator?,
        updateListener: ValueAnimator.AnimatorUpdateListener?
    ) {
        mUpdateListeners[animator!!] = updateListener!!
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        setDrawBounds(bounds)
    }

    open fun setDrawBounds(drawBounds: Rect) {
        setDrawBounds(drawBounds.left, drawBounds.top, drawBounds.right, drawBounds.bottom)
    }

    open fun setDrawBounds(left: Int, top: Int, right: Int, bottom: Int) {
        drawBounds = Rect(left, top, right, bottom)
    }

    open fun postInvalidate() {
        invalidateSelf()
    }

    open fun getDrawBounds(): Rect? {
        return drawBounds
    }

    open fun getWidth(): Int {
        return drawBounds.width()
    }

    open fun getHeight(): Int {
        return drawBounds.height()
    }

    open fun centerX(): Int {
        return drawBounds.centerX()
    }

    open fun centerY(): Int {
        return drawBounds.centerY()
    }

    open fun exactCenterX(): Float {
        return drawBounds.exactCenterX()
    }

    open fun exactCenterY(): Float {
        return drawBounds.exactCenterY()
    }


}