package com.yd;

import android.app.Application;
import android.util.Log;
import android.view.WindowManager;

public class GpsApplication extends Application {
    @Override  
    public void onCreate() {   
        super.onCreate();   
        CrashHandler crashHandler = CrashHandler.getInstance();   
        //注册crashHandler   
        crashHandler.init(getApplicationContext());   
    }  
}
