package de.twoid.spotifystreamer.artist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.SpotifyArtist;
import de.twoid.spotifystreamer.player.PlayerBarFragment;
import de.twoid.spotifystreamer.player.PlayerBarFragment.OnPlayerBarStateChangeListener;
import de.twoid.spotifystreamer.player.PlayerService;
import de.twoid.spotifystreamer.util.ServiceUtils;

public class ArtistActivity extends AppCompatActivity implements OnPlayerBarStateChangeListener {

    public static final String EXTRA_SPOTIFY_ARTIST = "de.twoid.spotifystreamer.SPOTIFY_ARTIST";

    private View playerBarContainer;
    private PlayerBarFragment playerBarFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        playerBarContainer = findViewById(R.id.player_bar_container);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment artistFragment = fragmentManager.findFragmentByTag(ArtistFragment.TAG);

        if(artistFragment == null){
            artistFragment = ArtistFragment.getInstance((SpotifyArtist) getIntent().getExtras().getParcelable(EXTRA_SPOTIFY_ARTIST));
        }

        fragmentManager
                .beginTransaction()
                .replace(R.id.container, artistFragment, ArtistFragment.TAG)
                .commit();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(playerBarFragment == null && ServiceUtils.isServiceRunning(this, PlayerService.class)){
            FragmentManager fragmentManager = getSupportFragmentManager();
            playerBarFragment = (PlayerBarFragment) fragmentManager.findFragmentByTag(PlayerBarFragment.TAG);

            if(playerBarFragment == null){
                playerBarFragment = new PlayerBarFragment();
            }

            playerBarFragment.setPlayerBarStateChangeListener(this);
            fragmentManager.beginTransaction()
                    .replace(R.id.player_bar_container, playerBarFragment, PlayerBarFragment.TAG)
                    .commit();
            onShowPlayerBar();
        }

        if(playerBarFragment != null && !ServiceUtils.isServiceRunning(this, PlayerService.class)){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().remove(playerBarFragment);
            onHidePlayerBar();
        }
    }

    @Override
    protected void onDestroy(){
        if(playerBarFragment != null){
            playerBarFragment.setPlayerBarStateChangeListener(null);
            playerBarFragment = null;
        }

        super.onDestroy();
    }

    @Override
    public void setSupportActionBar(Toolbar toolbar){
        super.setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onHidePlayerBar(){
        if(playerBarContainer != null){
            playerBarContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onShowPlayerBar(){
        if(playerBarContainer != null){
            playerBarContainer.setVisibility(View.VISIBLE);
        }
    }
}
