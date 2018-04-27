package com.devbatch.ecommerce.communication.event;


import de.greenrobot.event.EventBus;

public final class BusProvider {
    //    public static Bus mEventBuss;
    public static EventBus mEventBuss;

    public static EventBus getInstance() {
        if (mEventBuss == null) {
//            mEventBuss = new Bus();
            mEventBuss = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).build();
        }
        return mEventBuss;
    }
}
