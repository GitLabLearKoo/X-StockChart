package com.example.sample.base;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

/**
 * @author weixia
 * @date 2019/1/10.
 */
public abstract class BaseApp extends Application {
    public static BaseApp mContext = null; //获取到主线程的上下文
    public static Handler mAppHandler = null; //获取到主线程的handler
    public static Looper mAppLooper = null; //获取到主线程的looper
    public static Thread mMainThead = null; //获取到主线程
    public static int mMainTheadId; //获取到主线程的id

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mAppHandler = new Handler();
        mAppLooper = getMainLooper();
        mMainThead = Thread.currentThread();
        mMainTheadId = android.os.Process.myTid();//主線程id
    }

    public static BaseApp getApp() {
        return mContext;
    }

    public static Handler getAppHandler() {
        return mAppHandler;
    }

    public static Looper getAppLooper() {
        return mAppLooper;
    }

    public static Thread getMainThread() {
        return mMainThead;
    }

    public static int getMainThreadId() {
        return mMainTheadId;
    }
}
