package com.bignerdranch.android.photogallery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * Created by sredorta on 10/13/2016.
 */
public class GalleryItem {
    //Map JSON strings to our variables
    @SerializedName("title")
    private String mCaption;

    @SerializedName("id")
    private String mId;

    @SerializedName("url_s")
    private String mUrl;

    @Override
    public String toString() {
        return mCaption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
    //Static method (only one per class) that parses JSon string to object
    public static GalleryItem parseJSON(String response) {
        Gson gson = new GsonBuilder().create();
        GalleryItem itemResponse = gson.fromJson(response, GalleryItem.class);
        return itemResponse;
    }

}
