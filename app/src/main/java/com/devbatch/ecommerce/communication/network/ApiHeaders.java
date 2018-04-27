package com.devbatch.ecommerce.communication.network;//package com.sadeeq.app.communication;
//
//import android.os.Build;
//import android.text.TextUtils;
//
//
//import com.sparkleyourcar.sparklers.utils.AccountUtils;
//
//import retrofit.RequestInterceptor;
//
//class ApiHeaders implements RequestInterceptor {
//
//    @Override public void intercept(RequestFacade request) {
//        //todo: get environment name from gradle flavors
////        String version = String.format("Patient;Feature;%s;%s",
////                Application.packageInfo(0).versionName,
////                Application.packageInfo(0).versionCode);
////        String[] deviceDetail = {
////                Build.MANUFACTURER,
////                Build.MODEL,
////                Integer.toString(Device.getScreenWidth()),
////                Integer.toString(Device.getScreenHeight()),
////                String.valueOf(Device.getDPI())
////        };
////
////        request.addHeader("UserResponse-Agent", "Spruce/Android");
////        request.addHeader("S-Version", version);
////        request.addHeader("S-Device-ID", Device.getID());
////        request.addHeader("S-OS", "android;" + Build.VERSION.RELEASE);
////        request.addHeader("S-Device", TextUtils.join(";", deviceDetail));
////
//        String token = AccountUtils.getApiToke();
//        if (!TextUtils.isEmpty(token)) {
//            request.addHeader("TOKEN", token);
//        }
//    }
//}