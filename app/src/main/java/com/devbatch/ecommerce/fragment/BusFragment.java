package com.devbatch.ecommerce.fragment;



import com.devbatch.ecommerce.communication.event.Event;
import com.devbatch.ecommerce.communication.event.EventRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class BusFragment extends NetworkingFragment {

    private Map<Class<?>, Event> mPendingEvents = new LinkedHashMap<Class<?>, Event>();
    private Map<Class<?>, EventRunnable> mEventRunnableMap = new HashMap<Class<?>, EventRunnable>();

    @Override
    public void onResume() {
        super.onResume();
        Collection<Event> values = mPendingEvents.values();
        for (Event event : values) {
            EventRunnable runnable = mEventRunnableMap.get(event.getClass());
            if (runnable != null) {
                runnable.run(event);
            }
        }
        mPendingEvents.clear();
    }

    protected void addEventRunnable(Class<?> clazz, EventRunnable eventRunnable) {
        mEventRunnableMap.put(clazz, eventRunnable);
    }

    protected void handleEvent(Event event) {
        if (isVisible() && isResumed()) {
            EventRunnable eventRunnable = mEventRunnableMap.get(event.getClass());
            if (eventRunnable != null) {
                eventRunnable.run(event);
            }
        } else {
            mPendingEvents.put(event.getClass(), event);// make sure to not add the event twice
        }
    }

}

