package de.twoid.spotifystreamer.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.PlayerSession;
import de.twoid.spotifystreamer.object.SpotifyImage;
import de.twoid.spotifystreamer.object.SpotifyTrack;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerBarFragment extends PlayerBaseFragment {

    public static final String TAG = "PlayerBarFragment";

    private TextView tvArtistName;
    private View controlsView;

    public static PlayerBarFragment getInstance(PlayerSession session){
        PlayerBarFragment fragment = new PlayerBarFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SESSION, session);
        fragment.setArguments(args);
        return fragment;
    }

    public PlayerBarFragment(){

    }

    @Override
    protected void onSessionRequested(){
        if(getView() != null){
            getView().setVisibility(View.GONE);
        }
    }

    @Override
    public void onTrackChanged(SpotifyTrack track){
        super.onTrackChanged(track);
        if(getView() != null && getView().getVisibility() != View.VISIBLE){
            getView().setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected int getLayoutId(){
        return R.layout.fragment_player_bar;
    }

    @Override
    protected void initViews(View root){
        super.initViews(root);
        tvArtistName = (TextView) root.findViewById(R.id.tv_artist_name);
        controlsView = root.findViewById(R.id.controls_layout);

        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_SESSION, session);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void updateControlsColors(){
        controlsView.setBackgroundColor(controlLayoutColor);
    }

    @Override
    protected SpotifyImage getImage(){
        if(hasImage()){
            return session.getCurrentTrack().getMediumImage();
        }

        return null;
    }

    protected void setTrackInfoToViews(SpotifyTrack track){
        super.setTrackInfoToViews(track);
        if(!areViewsCreated){
            return;
        }

        if(artist == null){
            tvArtistName.setText(R.string.unknown_artist);
        }else{
            tvArtistName.setText(artist.name);
        }
    }
}
