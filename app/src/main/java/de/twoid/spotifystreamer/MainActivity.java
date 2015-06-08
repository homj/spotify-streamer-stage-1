package de.twoid.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.twoid.spotifystreamer.artist.ArtistActivity;
import de.twoid.spotifystreamer.artist.ArtistFragment;
import de.twoid.spotifystreamer.artist.TracksFragment;
import de.twoid.spotifystreamer.object.SpotifyArtist;
import de.twoid.spotifystreamer.search.SearchFragment;
import de.twoid.spotifystreamer.search.SearchFragment.OnSpotifyArtistSelectedListener;

public class MainActivity extends AppCompatActivity implements OnSpotifyArtistSelectedListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.spotifyartist_detail_container) != null){
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((SearchFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.search_fragment))
                    .setActivateOnItemClick(true);
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if(!mTwoPane){
            getSupportActionBar().setElevation(0);
        }else{
            getSupportActionBar().setTitle(null);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings){
            return true;
        }

        return super.onOptionsItemSelected(item);
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
}
