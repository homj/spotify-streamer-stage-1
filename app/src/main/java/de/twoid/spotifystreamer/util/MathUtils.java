package de.twoid.spotifystreamer.util;

/**
 * Created by Johannes on 05.06.2015.
 */
public class MathUtils {

    public static int clamp(int value, int min, int max){
        if(value < min){
            return min;
        }

        if(value > max){
            return max;
        }

        return value;
    }
}
