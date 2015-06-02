package de.twoid.spotifystreamer.player;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.SpotifyTrack;
import de.twoid.spotifystreamer.player.PlayerFragment;

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_SPOTIFY_TRACK = "de.twoid.spotifystreamer.SPOTIFY_TRACK";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        PlayerFragment playerFragment = PlayerFragment.getInstance((SpotifyTrack) getIntent().getExtras().getParcelable(EXTRA_SPOTIFY_TRACK));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, playerFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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
    public boolean onSupportNavigateUp(){
        if(!super.onSupportNavigateUp()){
            finish();
        }

        return true;
    }
}
