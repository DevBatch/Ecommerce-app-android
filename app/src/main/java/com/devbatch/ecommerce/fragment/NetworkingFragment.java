package com.devbatch.ecommerce.fragment;


import android.support.v4.app.Fragment;

import com.devbatch.ecommerce.communication.network.Api;
import com.devbatch.ecommerce.communication.network.CallbacksManager;


public class NetworkingFragment extends Fragment {
    protected CallbacksManager callbacksManager = new CallbacksManager();

    @Override
    public void onResume() {
        super.onResume();
        callbacksManager.resumeAll();
    }

    @Override
    public void onDestroyView() {
        callbacksManager.pauseAll();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        callbacksManager.cancelAll();
        super.onDestroy();
    }

    protected Api apiFor(CallbacksManager.CancelableCallback<?> callback) {
        callbacksManager.addCallback(callback);
        return Api.SERVICE;
    }


//
//    protected Api apiForG_Places(CallbacksManager.CancelableCallback<?> callback) {
//        callbacksManager.addCallback(callback);
//        return Api.G_PLACE_SERVICES;
//    }


}
