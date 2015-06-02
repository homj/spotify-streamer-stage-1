package de.twoid.spotifystreamer.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import java.util.ArrayList;

import de.twoid.spotifystreamer.Error;
import de.twoid.spotifystreamer.Errors;
import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.SpotifyFragment;
import de.twoid.spotifystreamer.object.SpotifyArtist;
import de.twoid.spotifystreamer.widget.EmptyLayout;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends SpotifyFragment implements Callback<ArtistsPager>, Handler.Callback {

    private static final int MESSAGE_TYPE_ARTISTS = 0;
    private static final String KEY_SPOTIFY_ARTIST_LIST = "artist_list";

    private ArrayList<SpotifyArtist> artistList;
    private SearchAdapter searchAdapter;

    private SearchView searchView;
    private RecyclerView resultsRecyclerView;

    public SearchFragment(){

    }

    @Override
    protected int getLayoutId(){
        return R.layout.fragment_search;
    }

    @Override
    protected void setupResources(){
        mHandler = new Handler(this);
        searchAdapter = new SearchAdapter();
    }

    @Override
    protected void initViews(View root){
        searchView = (SearchView) root.findViewById(R.id.searchview);
        resultsRecyclerView = (RecyclerView) root.findViewById(R.id.results_recyclerview);
        setEmptyLayout((EmptyLayout) root.findViewById(R.id.empty_layout));
    }

    @Override
    protected void setupViews(){
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query){
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText){
                search(newText);
                return false;
            }
        });

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        resultsRecyclerView.setAdapter(searchAdapter);
    }

    private void search(String query){
        if(!isConnectedToInternet()){
            displayError(Errors.ERROR_NO_INTERNET);
            return;
        }

        displayLoading();
        spotify.searchArtists(query, new SpotifyCallback<ArtistsPager>() {
            @Override
            public int getSuccessMessageType(){
                return MESSAGE_TYPE_ARTISTS;
            }

            @Override
            public de.twoid.spotifystreamer.Error resolveError(SpotifyError error){
                return new Error(R.string.hint_search);
            }
        });
    }

    @Override
    public void success(ArtistsPager artistsPager, Response response){
        Message message = new Message();
        message.obj = artistsPager;
        message.what = MESSAGE_TYPE_ARTISTS;
        mHandler.sendMessage(message);
    }

    @Override
    public void failure(RetrofitError error){
        Message message = new Message();
        message.what = MESSAGE_TYPE_ERROR;
        mHandler.sendMessage(message);
    }

    @Override
    protected void onMessageReceived(int type, Message message){
        if(type == MESSAGE_TYPE_ARTISTS){
            if(message.obj != null && message.obj instanceof ArtistsPager){
                artistList = SpotifyArtist.toSpotifyArtistList(((ArtistsPager) message.obj).artists.items);
                searchAdapter.setArtists(artistList);

                if(artistList == null || artistList.isEmpty()){
                    displayError(Errors.ERROR_NO_ARTISTS);
                }else{
                    displayContent();
                }
            }else{
                displayError(Errors.ERROR_NO_ARTISTS);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(KEY_SPOTIFY_ARTIST_LIST, artistList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null){
            artistList = savedInstanceState.getParcelableArrayList(KEY_SPOTIFY_ARTIST_LIST);
            if(searchAdapter != null){
                searchAdapter.setArtists(artistList);
            }
        }
    }
}
