package de.twoid.spotifystreamer.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.twoid.spotifystreamer.util.MathUtils;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Johannes on 01.06.2015.
 */
public class SpotifyTrack implements Parcelable {

    public List<SpotifyArtistSimple> artists;
    public List<String> available_markets;
    public boolean is_playable;
    public SpotifyLinkedTrack linked_from;
    public int disc_number;
    public long duration_ms;
    public boolean explicit;
    public Map<String, String> external_urls;
    public String href;
    public String id;
    public String name;
    public String preview_url;
    public int track_number;
    public String type;
    public String uri;
    public SpotifyAlbumSimple album;
    public Map<String, String> external_ids;
    public int popularity;

    public SpotifyTrack(@NonNull kaaes.spotify.webapi.android.models.Track track){
        artists = SpotifyArtistSimple.toSpotifyArtistSimpleList(track.artists);
        available_markets = track.available_markets;
        is_playable = track.is_playable == null ? false : track.is_playable;
        linked_from = SpotifyLinkedTrack.from(track.linked_from);
        disc_number = track.disc_number;
        duration_ms = track.duration_ms;
        explicit = track.explicit;
        external_urls = track.external_urls;
        href = track.href;
        id = track.id;
        name = track.name;
        preview_url = track.preview_url;
        track_number = track.track_number;
        type = track.type;
        uri = track.uri;
        album = new SpotifyAlbumSimple(track.album);
        external_ids = track.external_ids;
        popularity = track.popularity == null ? 0 : track.popularity;
    }

    private SpotifyTrack(Parcel in){
        artists = in.createTypedArrayList(SpotifyArtistSimple.CREATOR);
        available_markets = in.createStringArrayList();
        is_playable = in.readInt() == 1;
        linked_from = in.readParcelable(SpotifyLinkedTrack.class.getClassLoader());
        disc_number = in.readInt();
        duration_ms = in.readLong();
        explicit = in.readInt() == 1;
        external_urls = new HashMap<>();
        in.readMap(external_urls, HashMap.class.getClassLoader());
        href = in.readString();
        id = in.readString();
        name = in.readString();
        preview_url = in.readString();
        track_number = in.readInt();
        type = in.readString();
        uri = in.readString();
        album = in.readParcelable(SpotifyAlbumSimple.class.getClassLoader());
        external_ids = new HashMap<>();
        in.readMap(external_ids, HashMap.class.getClassLoader());
        popularity = in.readInt();
    }

    public static SpotifyTrack from(Track track){
        if(track == null){
            return null;
        }

        return new SpotifyTrack(track);
    }

    public boolean hasImage(){
        return album != null && album.images != null && !album.images.isEmpty();
    }

    public SpotifyImage getLargestImage(){
        if(!hasImage()){
            return null;
        }

        return album.images.get(0);
    }

    public SpotifyImage getMediumImage(){
        if(!hasImage()){
            return null;
        }

        final int imageCount = album.images.size();
        if(imageCount == 0){
            return album.images.get(0);
        }

        return album.images.get(MathUtils.clamp(imageCount / 2, 1, imageCount) - 1);
    }

    public SpotifyImage getSmallestImage(){
        if(!hasImage()){
            return null;
        }

        return album.images.get(album.images.size() - 1);
    }

    public boolean hasArtists(){
        return artists != null && !artists.isEmpty();
    }

    public SpotifyArtistSimple getFirstArtist(){
        return artists.get(0);
    }

    public static final Creator<SpotifyTrack> CREATOR = new Creator<SpotifyTrack>() {
        @Override
        public SpotifyTrack createFromParcel(Parcel in){
            return new SpotifyTrack(in);
        }

        @Override
        public SpotifyTrack[] newArray(int size){
            return new SpotifyTrack[size];
        }
    };

    public int getPreviewDurationInMillis(){
        return 30000;
    }

    public long getDurationInMillis(){
        return duration_ms;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeTypedList(artists);
        dest.writeStringList(available_markets);
        dest.writeInt(is_playable ? 1 : 0);
        dest.writeParcelable(linked_from, flags);
        dest.writeInt(disc_number);
        dest.writeLong(duration_ms);
        dest.writeInt(explicit ? 1 : 0);
        dest.writeMap(external_urls);
        dest.writeString(href);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(preview_url);
        dest.writeInt(track_number);
        dest.writeString(type);
        dest.writeString(uri);
        dest.writeParcelable(album, flags);
        dest.writeMap(external_ids);
        dest.writeInt(popularity);
    }

    public static ArrayList<SpotifyTrack> toSpotifyTrackList(List<Track> tracks){
        ArrayList<SpotifyTrack> transformedTracks = new ArrayList<>(tracks.size());

        for(Track track : tracks){
            if(track != null){
                transformedTracks.add(new SpotifyTrack(track));
            }
        }

        return transformedTracks;
    }
}
