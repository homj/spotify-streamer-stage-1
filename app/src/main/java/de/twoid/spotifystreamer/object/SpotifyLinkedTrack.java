package de.twoid.spotifystreamer.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.models.LinkedTrack;

/**
 * Created by Johannes on 01.06.2015.
 */
public class SpotifyLinkedTrack implements Parcelable {

    public Map<String, String> external_urls;
    public String href;
    public String id;
    public String type;
    public String uri;

    public SpotifyLinkedTrack(@NonNull LinkedTrack linkedTrack){
        external_urls = linkedTrack.external_urls;
        href = linkedTrack.href;
        id = linkedTrack.id;
        type = linkedTrack.type;
        uri = linkedTrack.uri;
    }

    private SpotifyLinkedTrack(Parcel in){
        external_urls = new HashMap<>();
        in.readMap(external_urls, HashMap.class.getClassLoader());
        href = in.readString();
        id = in.readString();
        type = in.readString();
        uri = in.readString();
    }

    public static SpotifyLinkedTrack from(LinkedTrack linkedTrack){
        if(linkedTrack == null){
            return null;
        }

        return new SpotifyLinkedTrack(linkedTrack);
    }

    public static final Creator<SpotifyLinkedTrack> CREATOR = new Creator<SpotifyLinkedTrack>() {
        @Override
        public SpotifyLinkedTrack createFromParcel(Parcel in){
            return new SpotifyLinkedTrack(in);
        }

        @Override
        public SpotifyLinkedTrack[] newArray(int size){
            return new SpotifyLinkedTrack[size];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeMap(external_urls);
        dest.writeString(href);
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(uri);
    }
}
