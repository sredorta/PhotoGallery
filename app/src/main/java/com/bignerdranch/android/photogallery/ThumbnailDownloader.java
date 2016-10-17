package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by sredorta on 10/17/2016.
 */
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "SERGI:ThumbnailThread";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    //Handler to send back once Thumbnail is downloaded
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDonwloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }
    public void setThumbnailDonwloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDonwloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler)  {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);
        if (url == null) {
            mRequestMap.remove(target);
        } else {
            // Send the request message for new download
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }
    //Removes all messages from queue
    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }
    //Define what needs to be done when reading each message
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    // Assign obj to target
                    T target = (T) msg.obj;
                    handleRequest(target);

                }
            }
        };
    }
    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getURLBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url) {
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDonwloadListener.onThumbnailDownloaded(target,bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloadinig image");
        }
    }
}
