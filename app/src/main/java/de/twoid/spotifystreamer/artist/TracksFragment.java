package de.twoid.spotifystreamer.artist;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.twoid.spotifystreamer.Errors;
import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.SpotifyFragment;
import de.twoid.spotifystreamer.object.SpotifyArtist;
import de.twoid.spotifystreamer.object.SpotifyTrack;
import de.twoid.spotifystreamer.widget.EmptyLayout;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A placeholder fragment containing a simple view.
 */
public class TracksFragment extends SpotifyFragment {

    private static final String ARG_SPOTIFY_ARTIST = "de.twoid.spotifystreamer.ARTIST_ID";
    private static final String KEY_SPOTIFY_ARTIST = "artist";
    private static final String KEY_SPOTIFY_TRACK_LIST = "track_list";
    private static final int MESSAGE_TYPE_TRACKS = 0;

    private SpotifyArtist artist;
    private ArrayList<SpotifyTrack> trackList;
    private TrackAdapter trackAdapter;

    private Toolbar toolbar;
    private RecyclerView tracksRecyclerView;


    public static TracksFragment getInstance(SpotifyArtist artist){
        TracksFragment fragment = new TracksFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SPOTIFY_ARTIST, artist);
        fragment.setArguments(args);
        return fragment;
    }

    public TracksFragment(){

    }

    @Override
    protected int getLayoutId(){
        return R.layout.fragment_tracks;
    }

    @Override
    protected void setupResources(){
        trackAdapter = new TrackAdapter(R.string.top_tracks);
        artist = getArguments().getParcelable(ARG_SPOTIFY_ARTIST);


        if(!isConnectedToInternet()){
            displayError(Errors.ERROR_NO_INTERNET);
        }else{
            displayLoading();

            Map<String, Object> options = new HashMap<>(1);
            options.put("country", "DE");
            spotify.getArtistTopTrack(artist.id, options, new SpotifyCallback<Tracks>() {

                @Override
                public int getSuccessMessageType(){
                    return MESSAGE_TYPE_TRACKS;
                }

                @Override
                public de.twoid.spotifystreamer.Error resolveError(SpotifyError error){
                    return Errors.ERROR_NO_TRACKS;
                }
            });
        }
    }

    @Override
    protected void initViews(View root){
        toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        tracksRecyclerView = (RecyclerView) root.findViewById(R.id.tracks_recyclerview);
        setEmptyLayout((EmptyLayout) root.findViewById(R.id.empty_layout));
    }

    @Override
    protected void setupViews(){
        setArtistToToolbar();
        toolbar.setSubtitle(R.string.top_tracks);
        if(getActivity() != null && getActivity() instanceof AppCompatActivity){
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        tracksRecyclerView.setAdapter(trackAdapter);
    }

    @Override
    protected void onMessageReceived(int type, Message message){
        if(type == MESSAGE_TYPE_TRACKS){
            if(message.obj != null && message.obj instanceof Tracks){
                trackList = SpotifyTrack.toSpotifyTrackList(((Tracks) message.obj).tracks);
                trackAdapter.setTracks(trackList);
                setArtistToToolbar();
            }
        }
    }

    private void setArtistToToolbar(){
        if(toolbar == null){
            return;
        }

        if(artist == null){
            toolbar.setTitle(R.string.unknown_artist);
        }else{
            toolbar.setTitle(artist.name);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_SPOTIFY_ARTIST, artist);
        outState.putParcelableArrayList(KEY_SPOTIFY_TRACK_LIST, trackList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null){
            artist = savedInstanceState.getParcelable(KEY_SPOTIFY_ARTIST);
            trackList = savedInstanceState.getParcelableArrayList(KEY_SPOTIFY_TRACK_LIST);
            setArtistToToolbar();
            if(trackAdapter != null){
                trackAdapter.setTracks(trackList);
            }
        }
    }
}
