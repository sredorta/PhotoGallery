package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceManager;

/**
 * Created by sredorta on 10/17/2016.
 */
public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PREF_LAST_RESULT_ID = "lastResultId";
    private static final String PREF_PAGE_NUMBER = "pageNumber";
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_QUERY, null);
    }
    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_SEARCH_QUERY, query).apply();
    }

    //Get last ResultID
    public static String getLastResultId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LAST_RESULT_ID,null);
    }
    //Save last ResultID
    public static void setLastResultId(Context context, String lastResultId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_LAST_RESULT_ID,lastResultId).apply();
    }

    //Get last pageNumber
    public static int getPageNumber(Context context) {
        String return_string = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_PAGE_NUMBER,null);
        int return_value = (int) Integer.valueOf(return_string);

        return return_value;
    }
    //Save last pageNumber
    public static void setPageNumber(Context context, int pageNumber) {
        String pageNumberS = Integer.toString(pageNumber);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_PAGE_NUMBER,pageNumberS).apply();
    }

    //Get if alarm is on
    public static boolean isAlarmOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_IS_ALARM_ON,false);
    }
    //Save last pageNumber
    public static void setAlarmOn(Context context, boolean isOn) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_IS_ALARM_ON,isOn).apply();
    }


}
