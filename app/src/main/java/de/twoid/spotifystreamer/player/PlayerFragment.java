package de.twoid.spotifystreamer.player;

import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.twoid.spotifystreamer.BaseFragment;
import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.SpotifyArtistSimple;
import de.twoid.spotifystreamer.object.SpotifyImage;
import de.twoid.spotifystreamer.object.SpotifyTrack;
import de.twoid.spotifystreamer.util.UiUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends BaseFragment implements com.squareup.picasso.Callback, PaletteAsyncListener {

    private static final String ARG_SPOTIFY_TRACK = "de.twoid.spotifystreamer.SPOTIFY_TRACK";
    private static final String KEY_SPOTIFY_ARTIST = "artist";
    private static final String KEY_SPOTIFY_TRACK = "track";

    private int controlLayoutColorFallback;
    private int controlsColorFallback;
    private int controlLayoutColor;
    private int controlsColor;

    private SpotifyArtistSimple artist;
    private SpotifyTrack track;

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


    public static PlayerFragment getInstance(SpotifyTrack track){
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SPOTIFY_TRACK, track);
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
    protected void initResources(){
        controlLayoutColor = controlLayoutColorFallback = getResources().getColor(R.color.branding);
        controlsColor = controlsColorFallback = getResources().getColor(R.color.accent);
        track = getArguments().getParcelable(ARG_SPOTIFY_TRACK);
        artist = (track == null || track.artists.isEmpty()) ? null : track.artists.get(0);
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
        setupActionBar();

        if(getActivity() != null && getActivity() instanceof AppCompatActivity){
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.abc_ic_clear_mtrl_alpha);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                tvPassedTime.setText(DateUtils.formatElapsedTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar){

            }
        });

        tvTrackName.setText(track.name);
        int durationInSeconds = (int) (track.duration_ms / 1000);
        tvTotalTime.setText(DateUtils.formatElapsedTime(durationInSeconds));
        seekBar.setMax(durationInSeconds);
        loadImage();
    }

    private void loadImage(){
        if(ivPicture == null){
            return;
        }

        SpotifyImage image = getImage();

        if(image != null){
            ivPicture.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout(){
                    Picasso.with(getActivity())
                            .load(track.album.images.get(0).url)
                            .resize(ivPicture.getWidth(), ivPicture.getHeight())
                            .centerCrop()
                            .placeholder(R.drawable.ic_albumart_placeholder)
                            .into(ivPicture, PlayerFragment.this);

                    UiUtils.removeOnGlobalLayoutListener(ivPicture, this);
                }
            });
        }
    }

    private void setupActionBar(){
        if(toolbar == null){
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

    private SpotifyImage getImage(){
        if(track == null){
            return null;
        }

        if(track.album == null){
            return null;
        }

        if(track.album.images == null || track.album.images.isEmpty()){
            return null;
        }

        return track.album.images.get(0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_SPOTIFY_ARTIST, artist);
        outState.putParcelable(KEY_SPOTIFY_TRACK, track);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null){
            artist = savedInstanceState.getParcelable(KEY_SPOTIFY_ARTIST);
            track = savedInstanceState.getParcelable(KEY_SPOTIFY_TRACK);
            setupActionBar();
            loadImage();
        }
    }
}
