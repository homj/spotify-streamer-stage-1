package de.twoid.spotifystreamer.object;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Followers;

/**
 * Created by Johannes on 01.06.2015.
 */
public class SpotifyFollowers implements Parcelable {

    public String href;
    public int total;

    public SpotifyFollowers(Followers followers){
        href = followers.href;
        total = followers.total;
    }

    private SpotifyFollowers(Parcel in){
        href = in.readString();
        total = in.readInt();
    }

    public static SpotifyFollowers from(Followers followers){
        if(followers == null){
            return null;
        }

        return new SpotifyFollowers(followers);
    }

    public static final Creator<SpotifyFollowers> CREATOR = new Creator<SpotifyFollowers>() {
        @Override
        public SpotifyFollowers createFromParcel(Parcel in){
            return new SpotifyFollowers(in);
        }

        @Override
        public SpotifyFollowers[] newArray(int size){
            return new SpotifyFollowers[size];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(href);
        dest.writeInt(total);
    }
}
