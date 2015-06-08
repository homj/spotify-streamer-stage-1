package de.twoid.spotifystreamer.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Johannes on 07.06.2015.
 */
public class PlayerSession implements Parcelable {

    private SpotifyArtist artist;
    private ArrayList<SpotifyTrack> playlist;
    private int firstTrackPosition;
    private String sessionToken;
    private int currentTrackPosition;

    public PlayerSession(SpotifyArtist artist, ArrayList<SpotifyTrack> playlist, int firstTrackPosition){
        this.artist = artist;
        this.playlist = playlist;
        this.firstTrackPosition = currentTrackPosition = firstTrackPosition;
        sessionToken = (artist == null ? "?" : artist.id) + "_" + firstTrackPosition;
    }

    private PlayerSession(Parcel in){
        artist = in.readParcelable(SpotifyArtist.class.getClassLoader());
        playlist = in.createTypedArrayList(SpotifyTrack.CREATOR);
        firstTrackPosition = currentTrackPosition = in.readInt();
        sessionToken = in.readString();
    }

    public static final Creator<PlayerSession> CREATOR = new Creator<PlayerSession>() {
        @Override
        public PlayerSession createFromParcel(Parcel in){
            return new PlayerSession(in);
        }

        @Override
        public PlayerSession[] newArray(int size){
            return new PlayerSession[size];
        }
    };

    public SpotifyArtist getArtist(){
        return artist;
    }

    public ArrayList<SpotifyTrack> getPlaylist(){
        return playlist;
    }

    public int getTrackCount(){
        return playlist == null ? 0 : playlist.size();
    }

    public int getFirstTrackPosition(){
        return firstTrackPosition;
    }

    public String getSessionToken(){
        return sessionToken;
    }

    public SpotifyTrack getTrackAt(int position){
        if(playlist == null || playlist.isEmpty()){
            return null;
        }

        if(position < 0 || position >= playlist.size()){
            return null;
        }

        return playlist.get(position);
    }

    public int getCurrentTrackPosition(){
        return currentTrackPosition;
    }

    public int getNextTrackPosition(){
        int nextTrackPosition = currentTrackPosition + 1;

        if(nextTrackPosition >= playlist.size()){
            nextTrackPosition = 0;
        }
        return nextTrackPosition;
    }

    public int getPreviousTrackPosition(){
        int nextTrackPosition = currentTrackPosition - 1;

        if(nextTrackPosition < 0){
            nextTrackPosition = playlist.size() - 1;
        }
        return nextTrackPosition;
    }

    public SpotifyTrack getCurrentTrack(){
        return getTrackAt(currentTrackPosition);
    }

    public SpotifyTrack getNextTrack(){
        if(playlist == null || playlist.isEmpty()){
            return null;
        }

        currentTrackPosition = getNextTrackPosition();
        return getTrackAt(currentTrackPosition);
    }

    public SpotifyTrack getPreviousTrack(){
        if(playlist == null || playlist.isEmpty()){
            return null;
        }

        currentTrackPosition = getPreviousTrackPosition();
        return getTrackAt(currentTrackPosition);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeParcelable(artist, flags);
        dest.writeTypedList(playlist);
        dest.writeInt(firstTrackPosition);
        dest.writeString(sessionToken);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof PlayerSession){
            return sessionToken.equals(((PlayerSession) o).getSessionToken());
        }

        return super.equals(o);
    }
}
