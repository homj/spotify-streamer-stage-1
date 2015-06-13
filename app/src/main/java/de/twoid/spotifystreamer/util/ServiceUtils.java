package de.twoid.spotifystreamer.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

/**
 * Created by Johannes on 12.06.2015.
 */
public class ServiceUtils {

    /**
     * Code from http://stackoverflow.com/a/5921190/1974562
     */
    public static boolean isServiceRunning(Context context, Class serviceClass){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
