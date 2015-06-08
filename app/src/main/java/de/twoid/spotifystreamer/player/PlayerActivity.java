package de.twoid.spotifystreamer.player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.PlayerSession;
import de.twoid.spotifystreamer.object.SpotifyTrack;
import de.twoid.spotifystreamer.player.PlayerFragment;

public class PlayerActivity extends AppCompatActivity {
    public static final String EXTRA_SESSION = "de.twoid.spotifystreamer.extra.SESSION";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment playerFragment = fragmentManager.findFragmentByTag(PlayerFragment.TAG);

        if(playerFragment == null){
            Bundle extras = getIntent().getExtras();
            playerFragment = PlayerFragment.getInstance((PlayerSession) extras.getParcelable(EXTRA_SESSION));
        }

        fragmentManager
                .beginTransaction()
                .replace(R.id.container, playerFragment, PlayerFragment.TAG)
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

    @Override
    public void setSupportActionBar(Toolbar toolbar){
        super.setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeAsUpIndicator(R.drawable.abc_ic_clear_mtrl_alpha);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
