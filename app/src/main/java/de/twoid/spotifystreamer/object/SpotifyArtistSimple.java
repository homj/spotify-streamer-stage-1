package de.twoid.spotifystreamer.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;

/**
 * Created by Johannes on 01.06.2015.
 */
public class SpotifyArtistSimple implements Parcelable {

    public Map<String, String> external_urls;
    public String href;
    public String id;
    public String name;
    public String type;
    public String uri;

    public SpotifyArtistSimple(@NonNull ArtistSimple artistSimple){
        external_urls = artistSimple.external_urls;
        href = artistSimple.href;
        id = artistSimple.id;
        name = artistSimple.name;
        type = artistSimple.type;
        uri = artistSimple.uri;
    }

    protected SpotifyArtistSimple(Parcel in){
        external_urls = new HashMap<>();
        in.readMap(external_urls, HashMap.class.getClassLoader());
        href = in.readString();
        id = in.readString();
        name = in.readString();
        type = in.readString();
        uri = in.readString();
    }

    public static SpotifyArtistSimple from(ArtistSimple artistSimple){
        if(artistSimple == null){
            return null;
        }

        return new SpotifyArtistSimple(artistSimple);
    }

    public static final Creator<SpotifyArtistSimple> CREATOR = new Creator<SpotifyArtistSimple>() {
        @Override
        public SpotifyArtistSimple createFromParcel(Parcel in){
            return new SpotifyArtistSimple(in);
        }

        @Override
        public SpotifyArtistSimple[] newArray(int size){
            return new SpotifyArtistSimple[size];
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
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(uri);
    }

    public static ArrayList<SpotifyArtistSimple> toSpotifyArtistSimpleList(List<ArtistSimple> artistSimpleList){
        ArrayList<SpotifyArtistSimple> transformedArtistSimpleList = new ArrayList<>(artistSimpleList.size());

        for(ArtistSimple artistSimple : artistSimpleList){
            if(artistSimple != null){
                if(artistSimple instanceof Artist){
                    transformedArtistSimpleList.add(new SpotifyArtist((Artist) artistSimple));
                }else{
                    transformedArtistSimpleList.add(new SpotifyArtistSimple(artistSimple));
                }
            }
        }

        return transformedArtistSimpleList;
    }
}
