package de.twoid.spotifystreamer.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.AlbumSimple;

/**
 * Created by Johannes on 01.06.2015.
 */
public class SpotifyAlbumSimple implements Parcelable {

    public String album_type;
    public List<String> available_markets;
    public Map<String, String> external_urls;
    public String href;
    public String id;
    public List<SpotifyImage> images;
    public String name;
    public String type;
    public String uri;

    public SpotifyAlbumSimple(@NonNull AlbumSimple albumSimple){
        album_type = albumSimple.album_type;
        available_markets = albumSimple.available_markets;
        external_urls = albumSimple.external_urls;
        href = albumSimple.href;
        id = albumSimple.id;
        images = SpotifyImage.toSpotifyImageList(albumSimple.images);
        name = albumSimple.name;
        type = albumSimple.type;
        uri = albumSimple.uri;
    }

    private SpotifyAlbumSimple(Parcel in){
        album_type = in.readString();
        available_markets = in.createStringArrayList();
        external_urls = new HashMap<>();
        in.readMap(external_urls, HashMap.class.getClassLoader());
        href = in.readString();
        id = in.readString();
        images = in.createTypedArrayList(SpotifyImage.CREATOR);
        name = in.readString();
        type = in.readString();
        uri = in.readString();
    }

    public static SpotifyAlbumSimple from(AlbumSimple albumSimple){
        if(albumSimple == null){
            return null;
        }

        return new SpotifyAlbumSimple(albumSimple);
    }

    public static final Creator<SpotifyAlbumSimple> CREATOR = new Creator<SpotifyAlbumSimple>() {
        @Override
        public SpotifyAlbumSimple createFromParcel(Parcel in){
            return new SpotifyAlbumSimple(in);
        }

        @Override
        public SpotifyAlbumSimple[] newArray(int size){
            return new SpotifyAlbumSimple[size];
        }
    };

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(album_type);
        dest.writeStringList(available_markets);
        dest.writeMap(external_urls);
        dest.writeString(href);
        dest.writeString(id);
        dest.writeTypedList(images);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(uri);
    }
}
