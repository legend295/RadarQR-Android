package com.radarqr.dating.android.utility.mapanimation;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.radarqr.dating.android.R;

public class MapRipple {
    private GoogleMap mGoogleMap;
    private LatLng mLatLng, mPrevLatLng;
    private BitmapDescriptor mBackgroundImageDescriptor;  //ripple image.
    private float mTransparency = 0.5f;                   //transparency of image.
    private volatile double mDistance = 0;             //distance to which ripple should be shown in metres
    private int mNumberOfRipples = 1;                     //number of ripples to show, max = 4
    private int mFillColor = Color.TRANSPARENT;           //fill color of circle
    private int mStrokeColor = Color.BLACK;               //border color of circle
    private int mStrokeWidth = 10;                        //border width of circle
    private long mDurationBetweenTwoRipples = 4000;       //in microseconds.
    private long mRippleDuration = 12000;                 //in microseconds
    private ValueAnimator mAnimators[];
    private Handler mHandlers[];
    private GroundOverlay mGroundOverlays[];
    private GradientDrawable mBackground;
    private boolean isAnimationRunning = false;

    public MapRipple(GoogleMap googleMap, LatLng latLng, Context context) {
        mGoogleMap = googleMap;
        mLatLng = latLng;
        mPrevLatLng = latLng;
        mBackground = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.background_ripple);
        mAnimators = new ValueAnimator[4];
        mHandlers = new Handler[4];
        mGroundOverlays = new GroundOverlay[4];
    }

    /**
     * @param transparency sets transparency for background of circle
     */
    public MapRipple withTransparency(float transparency) {
        mTransparency = transparency;
        return this;
    }

    /**
     * @param distance sets radius distance for circle
     */
    public MapRipple withDistance(double distance) {
        /*if (distance < 200) {
            distance = 200;
        }*/
        mDistance = distance;
        return this;
    }

    /**
     * @param latLng sets position for center of circle
     */
    public MapRipple withLatLng(LatLng latLng) {
        mPrevLatLng = mLatLng;
        mLatLng = latLng;
        return this;
    }

    /**
     * @param numberOfRipples sets count of ripples
     */
    public MapRipple withNumberOfRipples(int numberOfRipples) {
        if (numberOfRipples > 4 || numberOfRipples < 1) {
            numberOfRipples = 4;
        }
        mNumberOfRipples = numberOfRipples;
        return this;
    }

    /**
     * @param fillColor sets fill color
     */
    public MapRipple withFillColor(int fillColor) {
        mFillColor = fillColor;
        return this;
    }

    /**
     * @param strokeColor sets stroke color
     */
    public MapRipple withStrokeColor(int strokeColor) {
        mStrokeColor = strokeColor;
        return this;
    }

    /**
     * @param strokeWidth sets stroke width
     */
    public MapRipple withStrokeWidth(int strokeWidth) {
        mStrokeWidth = strokeWidth;
        return this;
    }

    /**
     * @param durationBetweenTwoRipples sets duration before pulse tick animation
     */
    public MapRipple withDurationBetweenTwoRipples(long durationBetweenTwoRipples) {
        mDurationBetweenTwoRipples = durationBetweenTwoRipples;
        return this;
    }

    /**
     * @return current state of animation
     */
    public boolean isAnimationRunning() {
        return isAnimationRunning;
    }

    /**
     * @param rippleDuration sets duration of ripple animation
     */
    public MapRipple withRippleDuration(long rippleDuration) {
        mRippleDuration = rippleDuration;
        return this;
    }

    private final Runnable mCircleOneRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("OVERLAY", "" + mDistance);
            mGroundOverlays[0] = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
                    .position(mLatLng, (int) mDistance)
                    .transparency(mTransparency)
                    .image(mBackgroundImageDescriptor));
            startAnimation(0);
        }
    };

    private final Runnable mCircleTwoRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("OVERLAY", "" + mDistance);
            mGroundOverlays[1] = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
                    .position(mLatLng, (int) mDistance)
                    .transparency(mTransparency)
                    .image(mBackgroundImageDescriptor));
            startAnimation(1);
        }
    };

    private final Runnable mCircleThreeRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("OVERLAY", "" + mDistance);
            mGroundOverlays[2] = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
                    .position(mLatLng, (int) mDistance)
                    .transparency(mTransparency)
                    .image(mBackgroundImageDescriptor));
            startAnimation(2);
        }
    };

    private final Runnable mCircleFourRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("OVERLAY", "" + mDistance);
            mGroundOverlays[3] = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
                    .position(mLatLng, (int) mDistance)
                    .transparency(mTransparency)
                    .image(mBackgroundImageDescriptor));
            startAnimation(3);
        }
    };

    protected double getDisplayPulseRadius(double radius) {
        double diff = (mGoogleMap.getMaxZoomLevel() - mGoogleMap.getCameraPosition().zoom);
        Log.d("CLUSTER","" + diff);
        if (diff < 3)
            return radius;
        if (diff < 3.7)
            return radius * (diff / 2);
        if (diff < 4.5)
            return (radius * diff);
        if (diff < 5.5)
            return (radius * diff) * 1.5f;
        if (diff < 7)
            return (radius * diff) * 2f;
        if (diff < 7.8)
            return (radius * diff) * 3.5f;
        if (diff < 8.5)
            return (float) (radius * diff) * 5;
        if (diff < 10)
            return (radius * diff) * 10f;
        if (diff < 12)
            return (radius * diff) * 21f;
        if (diff < 13)
            return (radius * diff) * 38f;
        if (diff < 16)
            return (radius * diff) * 70f;
        if (diff < 17)
            return (radius * diff) * 80f;
        if (diff < 18)
            return (radius * diff) * 90;
        return (radius * diff) * 150;
    }

    private void startAnimation(final int numberOfRipple) {
        ValueAnimator animator = ValueAnimator.ofInt(0, (int) getDisplayPulseRadius(mDistance) );
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setDuration(1000);
        animator.setEvaluator(new IntEvaluator());
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animated = (int) valueAnimator.getAnimatedValue();
                mGroundOverlays[numberOfRipple].setDimensions((float) getDisplayPulseRadius(animated));
                if (mDistance - animated <= 10) {
                }
                if (mLatLng != mPrevLatLng) {
                    mGroundOverlays[numberOfRipple].setPosition(mLatLng);
                }
            }
        });
        animator.start();
        mAnimators[numberOfRipple] = animator;
    }

    private void setDrawableAndBitmap() {
        mBackground.setColor(mFillColor);
        mBackground.setStroke(UiUtil.dpToPx(mStrokeWidth), mStrokeColor);
        mBackgroundImageDescriptor = UiUtil.drawableToBitmapDescriptor(mBackground);
    }

    /**
     * Stops current animation if it running
     */
    public void stopRippleMapAnimation() {
        if (isAnimationRunning) {
            try {
                for (int i = 0; i < mNumberOfRipples; i++) {
                    if (i == 0) {
                        mHandlers[i].removeCallbacks(mCircleOneRunnable);
                        mAnimators[i].cancel();
                        mGroundOverlays[i].remove();
                    }
                    if (i == 1) {
                        mHandlers[i].removeCallbacks(mCircleTwoRunnable);
                        mAnimators[i].cancel();
                        mGroundOverlays[i].remove();
                    }
                    if (i == 2) {
                        mHandlers[i].removeCallbacks(mCircleThreeRunnable);
                        mAnimators[i].cancel();
                        mGroundOverlays[i].remove();
                    }
                    if (i == 3) {
                        mHandlers[i].removeCallbacks(mCircleFourRunnable);
                        mAnimators[i].cancel();
                        mGroundOverlays[i].remove();
                    }
                }
            } catch (Exception e) {
                //no need to handle it
            }
        }
        isAnimationRunning = false;
    }

    /**
     * Starts animations
     */
    public void startRippleMapAnimation() {
        if (!isAnimationRunning) {
            setDrawableAndBitmap();
            for (int i = 0; i < mNumberOfRipples; i++) {
                if (i == 0) {
                    mHandlers[i] = new Handler();
                    mHandlers[i].postDelayed(mCircleOneRunnable, mDurationBetweenTwoRipples * i);
                }
                if (i == 1) {
                    mHandlers[i] = new Handler();
                    mHandlers[i].postDelayed(mCircleTwoRunnable, mDurationBetweenTwoRipples * i);
                }
                if (i == 2) {
                    mHandlers[i] = new Handler();
                    mHandlers[i].postDelayed(mCircleThreeRunnable, mDurationBetweenTwoRipples * i);
                }
                if (i == 3) {
                    mHandlers[i] = new Handler();
                    mHandlers[i].postDelayed(mCircleFourRunnable, mDurationBetweenTwoRipples * i);
                }
            }
        }
        isAnimationRunning = true;
    }
}
