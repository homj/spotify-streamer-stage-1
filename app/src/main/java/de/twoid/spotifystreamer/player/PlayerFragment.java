package de.twoid.spotifystreamer.player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.media.session.MediaSession;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.twoid.spotifystreamer.BaseFragment;
import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.PlayerSession;
import de.twoid.spotifystreamer.object.SpotifyArtist;
import de.twoid.spotifystreamer.object.SpotifyArtistSimple;
import de.twoid.spotifystreamer.object.SpotifyImage;
import de.twoid.spotifystreamer.object.SpotifyTrack;
import de.twoid.spotifystreamer.player.PlayerService.Callback;
import de.twoid.spotifystreamer.player.PlayerService.SpotifyStreamingBinder;
import de.twoid.spotifystreamer.player.StatefulMediaPlayer.State;
import de.twoid.spotifystreamer.util.UiUtils;

import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_NONE;
import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_PAUSED;
import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_STARTED;
import static de.twoid.spotifystreamer.player.StatefulMediaPlayer.STATE_STOPPED;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends BaseFragment implements com.squareup.picasso.Callback, PaletteAsyncListener, Callback {
    public static final String TAG = "PlayerFragment";
    private static final String ARG_SESSION = "de.twoid.spotifystreamer.SESSION";
    private static final String STATE_SESSION = "session";

    private int controlLayoutColorFallback;
    private int controlsColorFallback;
    private int controlLayoutColor;
    private int controlsColor;

    private PlayerSession session;
    private SpotifyTrack currentTrack;
    private SpotifyArtistSimple artist;

    private Toolbar toolbar;
    private ImageView ivPicture;
    private TextView tvTrackName;
    private CardView controlsView;
    private SeekBar seekBar;
    private TextView tvPassedTime;
    private TextView tvTotalTime;
    private ImageView ivSkipPrevious;
    private ImageView ivPlayPause;
    private ImageView ivSkipNext;


    private PlayerService streamingService;
    private boolean serviceBound = false;
    private Intent serviceIntent;
    private boolean isPlaying = false;
    private boolean areViewsCreated = false;
    private @State int currentPlayerState = STATE_NONE;
    private boolean recreated = false;

    //connect to the service
    private ServiceConnection streamingConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SpotifyStreamingBinder binder = (SpotifyStreamingBinder) service;
            //get service
            streamingService = binder.getService();

            PlayerSession sessionAtService = streamingService.getSession();

            if(sessionAtService != null && sessionAtService.equals(session)){
                session = sessionAtService;
            }else{
                streamingService.setSession(session, true);
            }
            setTrackInfoToViews(streamingService.getCurrentTrack());

            streamingService.registerCallback(PlayerFragment.this);

            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    public static PlayerFragment getInstance(PlayerSession session){
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SESSION, session);
        fragment.setArguments(args);
        return fragment;
    }

    public PlayerFragment(){

    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(serviceIntent == null){
            serviceIntent = new Intent(activity, PlayerService.class);
            activity.bindService(serviceIntent, streamingConnection, Context.BIND_AUTO_CREATE);
            activity.startService(serviceIntent);
        }
    }

    @Override
    public void onDestroyView(){
        areViewsCreated = false;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        unbindService();
        super.onDestroy();
    }

    public void unbindService(){
        if(streamingService != null){
            streamingService.unregisterCallback(null);
            getActivity().unbindService(streamingConnection);
            streamingService = null;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        areViewsCreated = true;
        super.onViewCreated(view, savedInstanceState);
        onPlayerStateChanged(currentPlayerState);
    }

    @Override
    protected int getLayoutId(){
        return R.layout.fragment_player;
    }

    @Override
    protected void initResources(){
        controlLayoutColor = controlLayoutColorFallback = getResources().getColor(R.color.branding);
        controlsColor = controlsColorFallback = getResources().getColor(R.color.accent);

        Bundle arguments = getArguments();

        if(arguments.containsKey(ARG_SESSION)){
            session = arguments.getParcelable(ARG_SESSION);
            setTrack(session.getCurrentTrack());
        }
    }

    @Override
    protected void setupResources(){

    }

    @Override
    protected void initViews(View root){
        toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        ivPicture = (ImageView) root.findViewById(R.id.iv_picture);

        tvTrackName = (TextView) root.findViewById(R.id.tv_track_name);
        controlsView = (CardView) root.findViewById(R.id.controls_layout);
        seekBar = (SeekBar) root.findViewById(R.id.seekbar);
        tvPassedTime = (TextView) root.findViewById(R.id.tv_passed_time);
        tvTotalTime = (TextView) root.findViewById(R.id.tv_total_time);
        ivSkipPrevious = (ImageView) root.findViewById(R.id.iv_skip_previous);
        ivPlayPause = (ImageView) root.findViewById(R.id.iv_play_pause);
        ivSkipNext = (ImageView) root.findViewById(R.id.iv_skip_next);
    }

    @Override
    protected void setupViews(){
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                unbindService();
            }
        });

        if(getActivity() != null && getActivity() instanceof AppCompatActivity){
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
        }

        ivSkipPrevious.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                skipToPrevious();
            }
        });

        ivPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                playPause();
            }
        });

        ivSkipNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                skipToNext();
            }
        });

        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            private boolean userInititaed = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                seekTo(progress, userInititaed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){
                userInititaed = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar){
                userInititaed = false;
            }
        });
    }

    private void setTrack(SpotifyTrack track){
        artist = track.hasArtists() ? track.getFirstArtist() : session.getArtist();

        if(areViewsCreated){
            setTrackInfoToViews(track);
        }
    }

    public void skipToPrevious(){
        if(streamingService != null){
            streamingService.skipToPrevious();
        }
    }

    public void skipToNext(){
        if(streamingService != null){
            streamingService.skipToNext();
        }
    }

    public void playPause(){
        isPlaying = !isPlaying;

        if(streamingService != null){
            streamingService.togglePlayPause();
        }
    }

    public void seekTo(int milliseconds, boolean userInitiated){
        tvPassedTime.setText(DateUtils.formatElapsedTime(milliseconds / 1000));
        if(userInitiated && streamingService != null){
            streamingService.seekTo(milliseconds);
        }
    }

    private void setProgress(int milliseconds){
        seekBar.setProgress(milliseconds);
    }

    private void loadImage(){
        if(ivPicture == null){
            return;
        }

        if(hasImage()){
            ivPicture.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout(){
                    Picasso.with(getActivity())
                            .load(getImage().url)
                            .resize(ivPicture.getWidth(), ivPicture.getHeight())
                            .centerCrop()
                            .placeholder(R.drawable.ic_albumart_placeholder)
                            .into(ivPicture, PlayerFragment.this);

                    UiUtils.removeOnGlobalLayoutListener(ivPicture, this);
                }
            });
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
        controlLayoutColor = palette.getDarkMutedColor(controlLayoutColorFallback);
        controlsColor = palette.getVibrantColor(controlsColorFallback);

        if(controlLayoutColor == controlLayoutColorFallback){
            controlLayoutColor = palette.getMutedColor(controlLayoutColorFallback);
            controlsColor = palette.getLightVibrantColor(controlsColor);
        }

        updateControlsColors();
    }

    private void updateControlsColors(){
        controlsView.setCardBackgroundColor(controlLayoutColor);

        ColorFilter colorFilter = new PorterDuffColorFilter(controlsColor, Mode.MULTIPLY);
        seekBar.getProgressDrawable().setColorFilter(colorFilter);

        if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP){
            seekBar.getThumb().setTint(controlsColor);
        }

        if(isAdded() && VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP){
            getActivity().getWindow().setStatusBarColor(controlLayoutColor);
        }
    }

    private boolean hasImage(){
        if(session == null){
            return false;
        }

        if(session.getCurrentTrack() == null){
            return false;
        }

        return session.getCurrentTrack().hasImage();
    }

    private SpotifyImage getImage(){
        if(hasImage()){
            return session.getCurrentTrack().getLargestImage();
        }

        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_SESSION, session);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null && savedInstanceState.containsKey(ARG_SESSION)){
            session = savedInstanceState.getParcelable(ARG_SESSION);
            if(session != null){
                setTrack(session.getCurrentTrack());
            }
        }
    }

    @Override
    public void onProgressChange(int progress){
        setProgress(progress);
    }

    @Override
    public void onTrackChanged(SpotifyTrack track){
        setTrack(track);
    }

    @Override
    public void onPlaybackStopped(){
        ivPlayPause.setImageResource(R.drawable.ic_play);
        isPlaying = false;
    }

    @Override
    public void onPlayerStateChanged(@State int newState){
        currentPlayerState = newState;

        if(!areViewsCreated){
            return;
        }

        switch(newState){
            case STATE_STARTED:
                ivPlayPause.setImageResource(R.drawable.ic_pause);
                break;
            case STATE_PAUSED:
                ivPlayPause.setImageResource(R.drawable.ic_play);
                break;
            case STATE_STOPPED:
                ivPlayPause.setImageResource(R.drawable.ic_play);
                seekTo(0, false);
                break;
        }
    }

    private void setTrackInfoToViews(SpotifyTrack track){
        if(!areViewsCreated){
            return;
        }

        if(artist == null){
            toolbar.setTitle(R.string.unknown_artist);
        }else{
            toolbar.setTitle(artist.name);
        }

        if(track == null){
            toolbar.setSubtitle(null);
        }else{
            toolbar.setSubtitle(track.album == null ? null : track.album.name);
        }

        tvTrackName.setText(track.name);
        tvTotalTime.setText(DateUtils.formatElapsedTime(track.getPreviewDurationInMillis() / 1000));
        seekBar.setMax(track.getPreviewDurationInMillis());
        loadImage();
    }
}
