package de.twoid.spotifystreamer.player;

import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.PlayerSession;
import de.twoid.spotifystreamer.object.SpotifyImage;
import de.twoid.spotifystreamer.object.SpotifyTrack;
import de.twoid.spotifystreamer.player.PlayerService.Callback;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends PlayerBaseFragment implements com.squareup.picasso.Callback, PaletteAsyncListener, Callback {

    public static final String TAG = "PlayerFragment";

    private Toolbar toolbar;
    private CardView controlsView;
    private SeekBar seekBar;
    private TextView tvPassedTime;
    private TextView tvTotalTime;


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
    protected int getLayoutId(){
        return R.layout.fragment_player;
    }

    @Override
    protected void initViews(View root){
        super.initViews(root);
        toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        controlsView = (CardView) root.findViewById(R.id.controls_layout);
        seekBar = (SeekBar) root.findViewById(R.id.seekbar);
        tvPassedTime = (TextView) root.findViewById(R.id.tv_passed_time);
        tvTotalTime = (TextView) root.findViewById(R.id.tv_total_time);
    }

    @Override
    protected void setupViews(){
        super.setupViews();
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

    public void seekTo(int milliseconds, boolean userInitiated){
        tvPassedTime.setText(DateUtils.formatElapsedTime(milliseconds / 1000));
        super.seekTo(milliseconds, userInitiated);
    }

    @Override
    protected void setProgress(int milliseconds){
        seekBar.setProgress(milliseconds);
    }

    @Override
    protected void updateControlsColors(){
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

    @Override
    protected SpotifyImage getImage(){
        if(hasImage()){
            return session.getCurrentTrack().getLargestImage();
        }

        return null;
    }

    @Override
    protected void setTrackInfoToViews(SpotifyTrack track){
        super.setTrackInfoToViews(track);
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
            tvTotalTime.setText(DateUtils.formatElapsedTime(0));
        }else{
            toolbar.setSubtitle(track.album == null ? null : track.album.name);
            tvTotalTime.setText(DateUtils.formatElapsedTime(track.getPreviewDurationInMillis() / 1000));
            seekBar.setMax(track.getPreviewDurationInMillis());
        }
    }

    @Override
    protected void onSessionRequested(){
        if(streamingService != null){
            if(session == null){
                session = getArguments().getParcelable(ARG_SESSION);
            }
            streamingService.setSession(session, true);
        }
    }
}
