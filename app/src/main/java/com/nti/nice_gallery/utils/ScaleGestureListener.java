package com.nti.nice_gallery.utils;

import android.view.ScaleGestureDetector;
import androidx.annotation.NonNull;

import java.util.function.Consumer;

public class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    public static class PinchArgs {

        public final float scaleFactor;
        public final float focusX;
        public final float focusY;
        public final float span;
        public final float spanDelta;

        public PinchArgs(
                float scaleFactor,
                float focusX,
                float focusY,
                float span,
                float spanDelta
        ) {
            this.scaleFactor = scaleFactor;
            this.focusX = focusX;
            this.focusY = focusY;
            this.span = span;
            this.spanDelta = spanDelta;
        }
    }

    Consumer<PinchArgs> pinchDetectedListener;

    public ScaleGestureListener(Consumer<PinchArgs> pinchDetectedListener) {
        this.pinchDetectedListener = pinchDetectedListener;
    }

    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        if (pinchDetectedListener != null) {
            float scaleFactor = detector.getScaleFactor();
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            float span = detector.getCurrentSpan();
            float spanDelta = detector.getCurrentSpan() - detector.getPreviousSpan();

            PinchArgs pinchArgs = new PinchArgs(
                    scaleFactor,
                    focusX,
                    focusY,
                    span,
                    spanDelta
            );

            pinchDetectedListener.accept(pinchArgs);
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
        return onScale(detector);
    }

    @Override
    public void onScaleEnd(@NonNull ScaleGestureDetector detector) {}
}
