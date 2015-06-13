package de.twoid.spotifystreamer.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;


/**
 * Created by Johannes on 01.06.2015.
 */
public class SpotifyArtist extends SpotifyArtistSimple implements Parcelable {

    public SpotifyFollowers followers;
    public List<String> genres;
    public List<SpotifyImage> images;
    public int popularity;

    public SpotifyArtist(@NonNull Artist artist){
        super(artist);
        followers = new SpotifyFollowers(artist.followers);
        genres = artist.genres;
        images = SpotifyImage.toSpotifyImageList(artist.images);
        popularity = artist.popularity == null ? 0 : artist.popularity;
    }

    private SpotifyArtist(Parcel in){
        super(in);
        followers = in.readParcelable(SpotifyFollowers.class.getClassLoader());
        genres = in.createStringArrayList();
        images = in.createTypedArrayList(SpotifyImage.CREATOR);
        popularity = in.readInt();
    }

    public static SpotifyArtist from(Artist artist){
        if(artist == null){
            return null;
        }

        return new SpotifyArtist(artist);
    }

    public boolean hasImage(){
        return images != null && !images.isEmpty();
    }

    public SpotifyImage getLargestImage(){
        if(!hasImage()){
            return null;
        }

        return images.get(0);
    }

    public SpotifyImage getSmallestImage(){
        if(!hasImage()){
            return null;
        }

        return images.get(images.size() - 1);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        super.writeToParcel(dest, flags);
        dest.writeParcelable(followers, flags);
        dest.writeStringList(genres);
        dest.writeTypedList(images);
        dest.writeInt(popularity);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    public static final Creator<SpotifyArtist> CREATOR = new Creator<SpotifyArtist>() {
        @Override
        public SpotifyArtist createFromParcel(Parcel in){
            return new SpotifyArtist(in);
        }

        @Override
        public SpotifyArtist[] newArray(int size){
            return new SpotifyArtist[size];
        }
    };

    public static ArrayList<SpotifyArtist> toSpotifyArtistList(List<Artist> artists){
        ArrayList<SpotifyArtist> transformedArtists = new ArrayList<>(artists.size());

        for(Artist artist : artists){
            if(artist != null){
                transformedArtists.add(new SpotifyArtist(artist));
            }
        }

        return transformedArtists;
    }
}
