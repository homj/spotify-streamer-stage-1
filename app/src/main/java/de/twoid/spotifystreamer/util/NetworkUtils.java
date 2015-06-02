package de.twoid.spotifystreamer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * Created by Johannes on 01.06.2015.
 */
public class NetworkUtils {

    public static boolean isConnected(Context context){
        boolean networkStatus;
        try{
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            networkStatus = isConnected(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)) || isConnected(cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI));
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

        return networkStatus;
    }

    public static boolean isConnected(NetworkInfo networkInfo){
        return networkInfo != null && networkInfo.getState() == State.CONNECTED;
    }

}
