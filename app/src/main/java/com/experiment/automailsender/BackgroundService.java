package com.experiment.automailsender;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

/**
 * Created by Sananda on 10-07-2018.
 */

public class MyService extends Service {
    private BroadcastReceiver mBroadcastReceiver = null;
    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcastReceiver = new MySMSBroadCastReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                registerReceiver(mBroadcastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
            }
        }).start();
        return START_REDELIVER_INTENT;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
