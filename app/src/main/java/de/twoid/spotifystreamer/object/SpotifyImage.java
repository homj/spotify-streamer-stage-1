package de.twoid.spotifystreamer.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Johannes on 01.06.2015.
 */
public class SpotifyImage implements Parcelable {

    public int width;
    public int height;
    public String url;

    public SpotifyImage(@NonNull Image image){
        width = image.width == null ? 0 : image.width;
        height = image.height == null ? 0 : image.height;
        url = image.url;
    }

    private SpotifyImage(Parcel in){
        width = in.readInt();
        height = in.readInt();
        url = in.readString();
    }

    public static SpotifyImage from(Image image){
        if(image == null){
            return null;
        }

        return new SpotifyImage(image);
    }

    public static final Creator<SpotifyImage> CREATOR = new Creator<SpotifyImage>() {
        @Override
        public SpotifyImage createFromParcel(Parcel in){
            return new SpotifyImage(in);
        }

        @Override
        public SpotifyImage[] newArray(int size){
            return new SpotifyImage[size];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(url);
    }

    public static ArrayList<SpotifyImage> toSpotifyImageList(List<Image> images){
        ArrayList<SpotifyImage> transformedImages = new ArrayList<>(images.size());

        for(Image image : images){
            if(image != null){
                transformedImages.add(new SpotifyImage(image));
            }
        }

        return transformedImages;
    }
}
