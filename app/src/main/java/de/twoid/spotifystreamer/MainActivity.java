package de.twoid.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import de.twoid.spotifystreamer.artist.ArtistActivity;
import de.twoid.spotifystreamer.artist.ArtistFragment;
import de.twoid.spotifystreamer.object.SpotifyArtist;
import de.twoid.spotifystreamer.player.PlayerBarFragment;
import de.twoid.spotifystreamer.player.PlayerBarFragment.OnPlayerBarStateChangeListener;
import de.twoid.spotifystreamer.player.PlayerService;
import de.twoid.spotifystreamer.search.SearchFragment;
import de.twoid.spotifystreamer.search.SearchFragment.OnSpotifyArtistSelectedListener;
import de.twoid.spotifystreamer.settings.SettingsActivity;
import de.twoid.spotifystreamer.util.ServiceUtils;

public class MainActivity extends AppCompatActivity implements OnSpotifyArtistSelectedListener, OnPlayerBarStateChangeListener {

    private boolean mTwoPane;
    private View playerBarContainer;
    private PlayerBarFragment playerBarFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerBarContainer = findViewById(R.id.player_bar_container);

        FragmentManager fragmentManager = getSupportFragmentManager();

        if(findViewById(R.id.spotifyartist_detail_container) != null){
            mTwoPane = true;

            ((SearchFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.search_fragment))
                    .setActivateOnItemClick(true);
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();

        if(!mTwoPane){
            if(actionBar != null){
                actionBar.setElevation(0);
            }
        }else{
            if(actionBar != null){
                actionBar.setTitle(null);
            }

            ArtistFragment artistFragment = (ArtistFragment) fragmentManager.findFragmentByTag(ArtistFragment.TAG);

            if(artistFragment == null){
                artistFragment = ArtistFragment.getInstance(null);
            }

            fragmentManager
                    .beginTransaction()
                    .replace(R.id.spotifyartist_detail_container, artistFragment, ArtistFragment.TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            fragmentManager.beginTransaction().remove(playerBarFragment).commit();
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
    public void onArtistSelected(SpotifyArtist artist){
        if(mTwoPane){
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            FragmentManager fragmentManager = getSupportFragmentManager();
            ArtistFragment artistFragment = (ArtistFragment) fragmentManager.findFragmentByTag(ArtistFragment.TAG);

            if(artistFragment == null){
                artistFragment = ArtistFragment.getInstance(artist);
            }else{
                artistFragment.setArtist(artist);
            }

            fragmentManager
                    .beginTransaction()
                    .replace(R.id.spotifyartist_detail_container, artistFragment, ArtistFragment.TAG)
                    .commit();

        }else{
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ArtistActivity.class);
            detailIntent.putExtra(ArtistActivity.EXTRA_SPOTIFY_ARTIST, artist);
            startActivity(detailIntent);
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
