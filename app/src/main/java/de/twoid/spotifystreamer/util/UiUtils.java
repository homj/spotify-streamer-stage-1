package de.twoid.spotifystreamer.util;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * Created by Johannes on 01.06.2015.
 */
public class UiUtils {

    public static Drawable getDrawable(@NonNull Resources resources, @DrawableRes int drawableResId){
        if(VERSION.SDK_INT < VERSION_CODES.LOLLIPOP_MR1){
            return resources.getDrawable(drawableResId);
        }else{
            return resources.getDrawable(drawableResId, null);
        }
    }


    public static void removeOnGlobalLayoutListener(View view, OnGlobalLayoutListener onGlobalLayoutListener){
        if(VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN){
            view.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
        }else{
            view.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }
}