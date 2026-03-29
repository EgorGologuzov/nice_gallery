package com.nti.nice_gallery.utils;

import java.util.function.Consumer;

public class CallbackShell {

    public static class ForRunnable {
        private Runnable callback;
        public Runnable getCallback() {
            return callback;
        }
        public void setCallback(Runnable callback) {
            this.callback = callback;
        }
        public void invokeCallback() {
            if (callback != null) {
                callback.run();
            }
        }
    }

    public static class ForConsumer<T> {
        private Consumer<T> callback;
        public Consumer<T> getCallback() {
            return callback;
        }
        public void setCallback(Consumer<T> callback) {
            this.callback = callback;
        }
        public void invokeCallback(T payload) {
            if (callback != null) {
                callback.accept(payload);
            }
        }
    }

}
