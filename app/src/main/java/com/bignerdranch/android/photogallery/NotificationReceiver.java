package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by sredorta on 10/19/2016.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "SERGI::NotificationR";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"received result: " + getResultCode());
        if (getResultCode() != Activity.RESULT_OK) {
            //A foreground activity has cancelled the diffusion
            return;
        }
        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE,0);
        Notification notification = (Notification) intent.getParcelableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(requestCode, notification);
    }
}
