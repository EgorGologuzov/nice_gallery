package com.nti.nice_gallery.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    public enum Gesture { Tap, DoubleTap, LongPress, SwipeLeft, SwipeRight, SwipeUp, SwipeDown, Scroll}

    public static class GestureArgs {
        public final Gesture gesture;
        public final Integer tapX;
        public final Integer tapY;
        public final Integer scrollDistX;
        public final Integer scrollDistY;

        public GestureArgs(
                Gesture gesture,
                Integer tapX,
                Integer tapY,
                Integer scrollDistX,
                Integer scrollDistY
        ) {
            this.gesture = gesture;
            this.tapX = tapX;
            this.tapY = tapY;
            this.scrollDistX = scrollDistX;
            this.scrollDistY = scrollDistY;
        }
    }

    private float downX;
    private float downY;
    private final Consumer<GestureArgs> gestureDetectedListener;

    public GestureListener(Consumer<GestureArgs> gestureDetectedListener) {
        this.gestureDetectedListener = gestureDetectedListener;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (gestureDetectedListener != null) {
            GestureArgs gestureArgs = new GestureArgs(
                    Gesture.Tap,
                    (int) e.getX(),
                    (int) e.getY(),
                    null,
                    null
            );

            gestureDetectedListener.accept(gestureArgs);
        }

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (gestureDetectedListener != null) {
            GestureArgs gestureArgs = new GestureArgs(
                    Gesture.DoubleTap,
                    (int) e.getX(),
                    (int) e.getY(),
                    null,
                    null
            );

            gestureDetectedListener.accept(gestureArgs);
        }

        return true;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        if (gestureDetectedListener != null) {
            GestureArgs gestureArgs = new GestureArgs(
                    Gesture.LongPress,
                    (int) e.getX(),
                    (int) e.getY(),
                    null,
                    null
            );

            gestureDetectedListener.accept(gestureArgs);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        final int SWIPE_THRESHOLD = 100;
        final int SWIPE_VELOCITY_THRESHOLD = 100;

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
            GestureArgs gestureArgs = new GestureArgs(
                    gesture,
                    null,
                    null,
                    null,
                    null
            );

            gestureDetectedListener.accept(gestureArgs);
            return true;
        }

        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        if (gestureDetectedListener != null) {
            GestureArgs gestureArgs = new GestureArgs(
                    Gesture.Scroll,
                    null,
                    null,
                    (int) -distanceX,
                    (int) -distanceY
            );

            gestureDetectedListener.accept(gestureArgs);
        }
        return true;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        downX = e.getX();
        downY = e.getY();
        return true;
    }
}
