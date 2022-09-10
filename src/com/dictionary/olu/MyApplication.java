package com.dictionary.olu;

import com.dictionary.olu.tool.CrashHandler;

import android.app.Application;

public class MyApplication extends Application {
    CrashHandler handler = null;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = CrashHandler.getInstance();
        handler.init(getApplicationContext());
    }
}
