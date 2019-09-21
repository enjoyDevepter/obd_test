package com.mapbar.adas;

import android.app.Application;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class AdasApplication extends Application {

    public static HUDInfo.HUDItem currentHUDItem;

    public static List<HUDInfo> hudInfos = new ArrayList<>();

    public static void registerUncaughtException() {
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                ex.printStackTrace();
                defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
    }


    @Override
    public void onCreate() {
        super.onCreate();
        GlobalUtil.setContext(this);
        GlobalUtil.setHandler(new Handler());
        registerUncaughtException();

        GlobalUtil.setOkHttpClient(new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build());
    }
}