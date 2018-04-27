package com.devbatch.ecommerce.communication.network;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class CallbacksManager {
    private Set<CancelableCallback> callbacks = new HashSet<>();

    public void cancelAll() {
        for (CancelableCallback callback : callbacks) {
            // false to avoid java.util.ConcurrentModificationException alternatively we can use
            // iterator
            callback.cancel(false);
        }
        callbacks.clear();
    }

    public void resumeAll() {
        final Iterator<CancelableCallback> iterator = callbacks.iterator();
        while (iterator.hasNext()) {
            boolean remove = iterator.next().resume();
            if (remove) {
                iterator.remove();
            }
        }
    }

    public void pauseAll() {
        for (CancelableCallback callback : callbacks) {
            callback.pause();
        }
    }

    public void addCallback(CancelableCallback<?> callback) {
        callbacks.add(callback);
    }

    private void removeCallback(CancelableCallback<?> callback) {
        callbacks.remove(callback);
    }

    public abstract class CancelableCallback<T> implements Callback<T> {
        private boolean canceled;
        private boolean paused;

        private T pendingT;
        private Response pendingResponse;
        private RetrofitError pendingError;

        public CancelableCallback() {
            this.canceled = false;
        }

        public void pause() {
            paused = true;
        }

        public boolean resume() {
            paused = false;
            // if callback was cancelled then no need to post pending results
            if (canceled) {
                return true;
            }
            if (pendingError != null) {
                onFailure(pendingError);
                // to make sure not to post it again
                pendingError = null;
                return true;
            } else if (pendingT != null) {
                onSuccess(pendingT, pendingResponse);
                // to make sure not to post it again
                pendingT = null;
                pendingResponse = null;
                return true;
            }
            return false;
        }

        public void cancel(boolean remove) {
            canceled = true;
            if (remove) {
                removeCallback(this);
            }
        }

        @Override
        public void success(T t, Response response) {
            if (canceled) {
                return;
            }
            if (paused) {
                pendingT = t;
                pendingResponse = response;
                return;
            }
            onSuccess(t, response);
            removeCallback(this);
        }

        @Override
        public void failure(RetrofitError error) {
            if (canceled) {
                return;
            }
            if (paused) {
                pendingError = error;
                return;
            }
            onFailure(error);
            removeCallback(this);
        }

        protected abstract void onSuccess(T t, Response response);

        protected abstract void onFailure(RetrofitError error);
    }
}
