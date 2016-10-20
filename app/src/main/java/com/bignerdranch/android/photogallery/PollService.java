package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by sredorta on 10/18/2016.
 */
public class PollService extends IntentService {
    private static final String TAG = "SERGI::PollService";
    private static final int POLL_INTERVAL = 1000*60*60; // 60 seconds
    public static final String ACTION_SHOW_NOTIFICATION = "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE= "com.bignerdranch.android.photogallery.PRIVATE";
    public static final String REQUEST_CODE ="REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    //Class constructor
    public PollService() {
        super(TAG);
    }

    //Define an alarm to check every X seconds if new content is available
    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
        QueryPreferences.setAlarmOn(context,isOn);
    }
    // Check if alarm is active
    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,PendingIntent.FLAG_NO_CREATE);
        return pi!= null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        //Log.i(TAG, "Received an intent: " + intent);
        String query        = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        int pageNumber  = QueryPreferences.getPageNumber(this);
        List<GalleryItem> items;
        //Toast.makeText(this,"Get Alarm Intent", Toast.LENGTH_SHORT);

        if (query == null) {
            items = new FlickrFetchr().fetchRecentPhotos(pageNumber);
        } else {
            items = new FlickrFetchr().searchPhotos(query,pageNumber);
        }
        if (items.size() == 0) {
            return;
        }
        String resultId = items.get(0).getId();
        if (resultId.equals(lastResultId)) {
            Log.d(TAG, "No new reload needed !");
            //Toast.makeText(getApplicationContext(),"No new reload", Toast.LENGTH_SHORT).show();
            //Got an old result, no reload necessary
        } else {
            Log.d(TAG, "Reload needed, send notification !");
             //Send notification that new data is there
            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this,0,i,0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
            showBackgroundNotification(0, notification);

            //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            //notificationManager.notify(0,notification);
            //Send broadcast intent each time new data is available
            //sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);

        }
        QueryPreferences.setLastResultId(this,resultId);
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE,null,null, Activity.RESULT_OK,null,null);
    }

    //Check that network is available
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = cm.getActiveNetworkInfo().isConnected() && isNetworkAvailable;
        return isNetworkConnected;
    }
}
