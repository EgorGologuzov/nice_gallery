package com.nti.nice_gallery.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    public enum Gesture { Tap, DoubleTap, LongPress, SwipeLeft, SwipeRight, SwipeUp, SwipeDown }

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private float downX;
    private float downY;
    private final Consumer<Gesture> gestureDetectedListener;

    public GestureListener(Consumer<Gesture> gestureDetectedListener) {
        this.gestureDetectedListener = gestureDetectedListener;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (gestureDetectedListener != null) {
            gestureDetectedListener.accept(Gesture.Tap);
        }

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (gestureDetectedListener != null) {
            gestureDetectedListener.accept(Gesture.DoubleTap);
        }

        return true;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        if (gestureDetectedListener != null) {
            gestureDetectedListener.accept(Gesture.LongPress);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - downX;
        float diffY = e2.getY() - downY;

        Gesture gesture = null;

        if (Math.abs(diffX) > Math.abs(diffY) &&
                Math.abs(diffX) > SWIPE_THRESHOLD &&
                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
        ) {
            gesture = diffX > 0 ? Gesture.SwipeRight : Gesture.SwipeLeft;
        }

        if (Math.abs(diffX) < Math.abs(diffY) &&
                Math.abs(diffY) > SWIPE_THRESHOLD &&
                Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD
        ) {
            gesture = diffY > 0 ? Gesture.SwipeDown : Gesture.SwipeUp;
        }

        if (gesture != null && gestureDetectedListener != null) {
            gestureDetectedListener.accept(gesture);
            return true;
        }

        return false;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        downX = e.getX();
        downY = e.getY();
        return true;
    }
}
