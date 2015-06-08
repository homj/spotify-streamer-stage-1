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

import de.twoid.spotifystreamer.BaseFragment;
import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.PlayerSession;
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
public abstract class PlayerBaseFragment extends BaseFragment implements com.squareup.picasso.Callback, PaletteAsyncListener, Callback {

    protected static final String ARG_SESSION = "de.twoid.spotifystreamer.SESSION";

    protected int controlLayoutColorFallback;
    protected int controlsColorFallback;
    protected int controlLayoutColor;
    protected int controlsColor;

    protected PlayerSession session;
    protected SpotifyArtistSimple artist;

    protected ImageView ivPicture;
    protected TextView tvTrackName;
    protected ImageView ivSkipPrevious;
    protected ImageView ivPlayPause;
    protected ImageView ivSkipNext;


    protected PlayerService streamingService;
    protected Intent serviceIntent;
    protected boolean isPlaying = false;
    protected boolean areViewsCreated = false;
    protected @State int currentPlayerState = STATE_NONE;

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
                onSessionRequested();
            }

            setTrackInfoToViews(streamingService.getCurrentTrack());

            streamingService.registerCallback(PlayerBaseFragment.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public PlayerBaseFragment(){

    }

    protected abstract void onSessionRequested();

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
    protected void initResources(){
        controlLayoutColor = controlLayoutColorFallback = getResources().getColor(R.color.branding);
        controlsColor = controlsColorFallback = getResources().getColor(R.color.accent);

        Bundle arguments = getArguments();

        if(arguments != null && arguments.containsKey(ARG_SESSION)){
            session = arguments.getParcelable(ARG_SESSION);
            setTrack(session.getCurrentTrack());
        }
    }

    @Override
    protected void setupResources(){

    }

    @Override
    protected void initViews(View root){
        ivPicture = (ImageView) root.findViewById(R.id.iv_picture);

        tvTrackName = (TextView) root.findViewById(R.id.tv_track_name);
        ivSkipPrevious = (ImageView) root.findViewById(R.id.iv_skip_previous);
        ivPlayPause = (ImageView) root.findViewById(R.id.iv_play_pause);
        ivSkipNext = (ImageView) root.findViewById(R.id.iv_skip_next);
    }

    @Override
    protected void setupViews(){
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
    }

    private void setTrack(SpotifyTrack track){
        session = streamingService == null ? null : streamingService.getSession();
        artist = track.hasArtists() ? track.getFirstArtist() : (session == null ? null : session.getArtist());

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
        if(userInitiated && streamingService != null){
            streamingService.seekTo(milliseconds);
        }
    }

    protected void setProgress(int milliseconds){

    }

    protected void loadImage(){
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
                            .into(ivPicture, PlayerBaseFragment.this);

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

    protected abstract void updateControlsColors();

    protected boolean hasImage(){
        if(session == null){
            return false;
        }

        if(session.getCurrentTrack() == null){
            return false;
        }

        return session.getCurrentTrack().hasImage();
    }

    protected SpotifyImage getImage(){
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

    protected void setTrackInfoToViews(SpotifyTrack track){
        if(!areViewsCreated){
            return;
        }

        if(track == null){
            tvTrackName.setText(null);
        }else{
            tvTrackName.setText(track.name);
        }

        loadImage();
    }
}
