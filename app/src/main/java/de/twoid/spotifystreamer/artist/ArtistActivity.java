package de.twoid.spotifystreamer.artist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.artist.TracksFragment;
import de.twoid.spotifystreamer.object.SpotifyArtist;

public class ArtistActivity extends AppCompatActivity {

    public static final String EXTRA_SPOTIFY_ARTIST = "de.twoid.spotifystreamer.SPOTIFY_ARTIST";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

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
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist, menu);
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
    public void setSupportActionBar(Toolbar toolbar){
        super.setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
