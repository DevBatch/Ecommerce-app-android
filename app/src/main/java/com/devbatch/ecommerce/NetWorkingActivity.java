package com.devbatch.ecommerce;


import com.devbatch.ecommerce.communication.network.Api;
import com.devbatch.ecommerce.communication.network.CallbacksManager;

public abstract class NetWorkingActivity extends BusActivity {
    protected CallbacksManager callbacksManager = new CallbacksManager();

    @Override
    public void onResume() {
        super.onResume();
        callbacksManager.resumeAll();
    }

    @Override
    public void onDestroy() {
        callbacksManager.pauseAll();
        callbacksManager.cancelAll();
        super.onDestroy();
    }

    protected Api apiFor(CallbacksManager.CancelableCallback<?> callback) {
        callbacksManager.addCallback(callback);
        return Api.SERVICE;
    }

}