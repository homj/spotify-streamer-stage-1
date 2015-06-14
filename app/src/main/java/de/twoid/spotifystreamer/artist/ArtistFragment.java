package de.twoid.spotifystreamer.artist;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.twoid.spotifystreamer.Messages;
import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.SpotifyFragment;
import de.twoid.spotifystreamer.object.PlayerSession;
import de.twoid.spotifystreamer.object.SpotifyArtist;
import de.twoid.spotifystreamer.object.SpotifyTrack;
import de.twoid.spotifystreamer.player.PlayerActivity;
import de.twoid.spotifystreamer.search.OnItemClickListener;
import de.twoid.spotifystreamer.settings.Preferences;
import de.twoid.spotifystreamer.widget.EmptyLayout;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistFragment extends SpotifyFragment implements Callback, PaletteAsyncListener, OnItemClickListener<SpotifyTrack>{
    public static final String TAG = "ArtistFragment";
    private static final String ARG_SPOTIFY_ARTIST = "de.twoid.spotifystreamer.ARTIST_ID";
    private static final String KEY_SPOTIFY_ARTIST = "artist";
    private static final String KEY_SPOTIFY_TRACK_LIST = "track_list";
    private static final int MESSAGE_TYPE_TRACKS = 0;

    private @ColorInt int toolbarColorFallback;
    private @ColorInt int toolbarColor;
    private SpotifyArtist artist;
    private ArrayList<SpotifyTrack> trackList;
    private TrackAdapter trackAdapter;

    private EmptyLayout rootEmptyLayout;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView ivPicture;
    private Toolbar toolbar;
    private RecyclerView tracksRecyclerView;


    public static ArtistFragment getInstance(SpotifyArtist artist){
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SPOTIFY_ARTIST, artist);
        fragment.setArguments(args);
        return fragment;
    }

    public ArtistFragment(){

    }

    @Override
    protected int getLayoutId(){
        return R.layout.fragment_artist;
    }

    @Override
    protected void initResources(){
        super.initResources();
        toolbarColor = toolbarColorFallback = getResources().getColor(R.color.accent);
        trackAdapter = new TrackAdapter(R.string.top_tracks);
    }

    @Override
    protected void setupResources(){
        trackAdapter.setOnItemClickListener(this);
    }

    public void setArtist(SpotifyArtist artist){
        if(artist == null){
            displayRootMessage(Messages.MESSAGE_NO_ARTIST_SELECTED);
            return;
        }else{
            displayRootContent();
        }

        if(this.artist == artist){
            return;
        }

        this.artist = artist;
        if(!isConnectedToInternet()){
            displayMessage(Messages.MESSAGE_NO_INTERNET);
        }else{
            displayLoading();

            Map<String, Object> options = new HashMap<>(1);
            String countryCode = Preferences.getCountryCode(getActivity());
            options.put("country", countryCode);
            Log.d("ArtistFragment", "country code: " + countryCode);
            spotify.getArtistTopTrack(artist.id, options, new SpotifyCallback<Tracks>() {

                @Override
                public int getSuccessMessageType(){
                    return MESSAGE_TYPE_TRACKS;
                }

                @Override
                public de.twoid.spotifystreamer.Message resolveError(SpotifyError error){
                    return Messages.MESSAGE_NO_TRACKS;
                }
            });
        }

            setArtistInfoToViews();
    }

    @Override
    protected void initViews(View root){
        rootEmptyLayout = (EmptyLayout) root.findViewById(R.id.root_empty_layout);
        toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        tracksRecyclerView = (RecyclerView) root.findViewById(R.id.tracks_recyclerview);
        setEmptyLayout((EmptyLayout) root.findViewById(R.id.empty_layout));

        collapsingToolbar = (CollapsingToolbarLayout) root.findViewById(R.id.collapsing_toolbar);

        ivPicture = (ImageView) root.findViewById(R.id.iv_picture);

    }

    @Override
    protected void setupViews(){
        rootEmptyLayout.setState(EmptyLayout.STATE_DISPLAY_MESSAGE);
        setArtist(getArguments() == null ? null : (SpotifyArtist) getArguments().getParcelable(ARG_SPOTIFY_ARTIST));
        rootEmptyLayout.setMessage(Messages.MESSAGE_NO_ARTIST_SELECTED);
        //        setArtistToToolbar();
        //        toolbar.setSubtitle(R.string.top_tracks);
        if(getActivity() != null && getActivity() instanceof AppCompatActivity){
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
        }

        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        tracksRecyclerView.setAdapter(trackAdapter);
        setArtistInfoToViews();
    }

    private void setArtistInfoToViews(){
        collapsingToolbar.setTitle(artist == null ? null : artist.name);

        if(artist == null){
            toolbar.setTitle(R.string.unknown_artist);
        }else{
            toolbar.setTitle(artist.name);
        }

        if(artist != null && artist.hasImage()){
            Picasso.with(getActivity())
                    .load(artist.getLargestImage().url)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_artist_placeholder)
                    .into(ivPicture, ArtistFragment.this);
        }else{
            Picasso.with(getActivity())
                    .load(R.drawable.ic_artist_placeholder)
                    .fit()
                    .centerCrop()
                    .into(ivPicture);
        }
    }

    @Override
    protected void onMessageReceived(int type, Message message){
        if(type == MESSAGE_TYPE_TRACKS){
            if(message.obj != null && message.obj instanceof Tracks){
                trackList = SpotifyTrack.toSpotifyTrackList(((Tracks) message.obj).tracks);
                trackAdapter.setTracks(trackList);
                displayContent();
            }
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
            setArtistInfoToViews();
            if(trackAdapter != null){
                trackAdapter.setTracks(trackList);
            }
        }
    }

    @Override
    public void onSuccess(){
        BitmapDrawable drawable = (BitmapDrawable) ivPicture.getDrawable();
        Palette.from(drawable.getBitmap()).generate(this);
    }

    @Override
    public void onError(){

    }

    @Override
    public void onGenerated(Palette palette){
        toolbarColor = palette.getDarkVibrantColor(toolbarColorFallback);

        if(toolbarColor == toolbarColorFallback){
            toolbarColor = palette.getVibrantColor(toolbarColorFallback);

            if(toolbarColor == toolbarColorFallback){
                toolbarColor = palette.getDarkMutedColor(toolbarColorFallback);

                if(toolbarColor == toolbarColorFallback){
                    toolbarColor = palette.getMutedColor(toolbarColorFallback);
                }
            }
        }

        collapsingToolbar.setContentScrimColor(toolbarColor);

        if(isAdded() && getActivity() instanceof ArtistActivity && VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP){

            float[] hsv = new float[3];
            Color.colorToHSV(toolbarColor, hsv);
            hsv[2] *= 0.8f; // value component
            int statusbarcolor = Color.HSVToColor(hsv);

            getActivity().getWindow().setStatusBarColor(statusbarcolor);
        }
    }

    @Override
    public void onItemClick(View view, SpotifyTrack track, int position){
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_SESSION, new PlayerSession(artist, trackList, trackList.indexOf(track)));
        startActivity(intent);
    }

    private void displayRootMessage(de.twoid.spotifystreamer.Message message){
        if(rootEmptyLayout != null){
            rootEmptyLayout.setMessage(message);
            rootEmptyLayout.setState(EmptyLayout.STATE_DISPLAY_MESSAGE);
        }
    }

    private void displayRootContent(){
        if(rootEmptyLayout != null){
            rootEmptyLayout.setState(EmptyLayout.STATE_DISPLAY_CONTENT);
        }
    }
}
