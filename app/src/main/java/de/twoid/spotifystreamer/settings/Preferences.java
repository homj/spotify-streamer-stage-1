package de.twoid.spotifystreamer.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Johannes on 14.06.2015.
 */
public class Preferences {
    public static final String KEY_COUNTRY = "pref_country_code";
    public static final String KEY_NOTIFICATION_VISIBILITY = "pref_notification_visibility";
    public static final String DEFAULT_COUNTRY_CODE = "DE";
    public static final boolean DEFAULT_NOTIFICATION_VISIBILITY = true;

    public static String getCountryCode(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(KEY_COUNTRY, DEFAULT_COUNTRY_CODE);
    }

    public static int getNotificationVisibility(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showNotificationOnLockscreen =  preferences.getBoolean(KEY_NOTIFICATION_VISIBILITY, DEFAULT_NOTIFICATION_VISIBILITY);

        return showNotificationOnLockscreen ? NotificationCompat.VISIBILITY_PUBLIC : NotificationCompat.VISIBILITY_SECRET;
    }
}
