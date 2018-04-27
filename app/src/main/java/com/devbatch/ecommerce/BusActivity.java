package com.devbatch.ecommerce;

import android.support.v7.app.AppCompatActivity;


import com.devbatch.ecommerce.communication.event.Event;
import com.devbatch.ecommerce.communication.event.EventRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class BusActivity extends AppCompatActivity {
    private Map<Class<?>, Event> mPendingEvents = new LinkedHashMap<Class<?>, Event>();
    private Map<Class<?>, EventRunnable> mEventRunnableMap = new HashMap<Class<?>, EventRunnable>();
    protected boolean mResumed;

    protected void addEventRunnable(Class<?> clazz, EventRunnable eventRunnable) {
        mEventRunnableMap.put(clazz, eventRunnable);
    }

    protected void handleEvent(Event event) {
        if (mResumed) {
            EventRunnable eventRunnable = mEventRunnableMap.get(event.getClass());
            if (eventRunnable != null) {
                eventRunnable.run(event);
            }
        } else {
            mPendingEvents.put(event.getClass(), event);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mResumed = true;
        Collection<Event> values = mPendingEvents.values();
        for (Event event : values) {
            EventRunnable runnable = mEventRunnableMap.get(event.getClass());
            if (runnable != null) {
                runnable.run(event);
            }
        }
        mPendingEvents.clear();
    }
}
