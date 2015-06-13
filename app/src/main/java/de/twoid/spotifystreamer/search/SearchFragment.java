package de.twoid.spotifystreamer.search;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import java.util.ArrayList;

import de.twoid.spotifystreamer.ItemListAdapter;
import de.twoid.spotifystreamer.Message;
import de.twoid.spotifystreamer.Messages;
import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.SpotifyFragment;
import de.twoid.spotifystreamer.object.SpotifyArtist;
import de.twoid.spotifystreamer.widget.EmptyLayout;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends SpotifyFragment implements Handler.Callback, OnItemClickListener<SpotifyArtist> {

    private static final int MESSAGE_TYPE_ARTISTS = 0;
    private static final int MESSAGE_TYPE_SEARCH = 100;
    private static final String STATE_QUERY = "query";
    private static final String STATE_SPOTIFY_ARTIST_LIST = "artist_list";
    private static final String STATE_LAST_SELECTED_POSITION = "last_selected_position";
    private static final long SEARCH_DELAY = 300;

    private ArrayList<SpotifyArtist> artistList;
    private SearchAdapter searchAdapter;

    private SearchView searchView;
    private RecyclerView resultsRecyclerView;
    private OnSpotifyArtistSelectedListener itemClickListener;
    private int lastSelectedItemPosition = -1;
    private boolean activateOnItemClick;
    private String lastQuery;

    public SearchFragment(){

    }

    @Override
    protected int getLayoutId(){
        return R.layout.fragment_search;
    }

    @Override
    protected void initResources(){
        super.initResources();
        mHandler = new Handler(this);
        searchAdapter = new SearchAdapter(artistList, lastSelectedItemPosition, this);
    }

    @Override
    protected void setupResources(){
        setActivateOnItemClick(activateOnItemClick);
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

    private void searchDelayed(String query){
        android.os.Message message = android.os.Message.obtain();
        message.what = MESSAGE_TYPE_SEARCH;
        message.obj = query;
        mHandler.removeMessages(MESSAGE_TYPE_SEARCH);
        mHandler.sendMessageDelayed(message, SEARCH_DELAY);
    }

    private void search(String query){
        if(query == null || query.equals(lastQuery)){
            return;
        }

        lastQuery = query;
        if(!isConnectedToInternet()){
            displayError(Messages.MESSAGE_NO_INTERNET);
            return;
        }

        displayLoading();
        spotify.searchArtists(query, new SpotifyCallback<ArtistsPager>() {
            @Override
            public int getSuccessMessageType(){
                return MESSAGE_TYPE_ARTISTS;
            }

            @Override
            public Message resolveError(SpotifyError error){
                return new Message(R.string.msg_search_hint);
            }
        });
    }

    @Override
    protected void onMessageReceived(int type, android.os.Message message){
        if(type == MESSAGE_TYPE_ARTISTS){
            if(message.obj != null && message.obj instanceof ArtistsPager){
                artistList = SpotifyArtist.toSpotifyArtistList(((ArtistsPager) message.obj).artists.items);
                searchAdapter.setArtists(artistList);

                if(artistList == null || artistList.isEmpty()){
                    displayError(Messages.MESSAGE_NO_ARTISTS);
                }else{
                    displayContent();
                }
            }else{
                displayError(Messages.MESSAGE_NO_ARTISTS);
            }
        }else if(type == MESSAGE_TYPE_SEARCH){
            search((String) message.obj);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putString(STATE_QUERY, lastQuery);
        outState.putParcelableArrayList(STATE_SPOTIFY_ARTIST_LIST, artistList);
        outState.putInt(STATE_LAST_SELECTED_POSITION, searchAdapter == null ? -1 : searchAdapter.getLastSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null){
            lastQuery = savedInstanceState.getString(STATE_QUERY);
            artistList = savedInstanceState.getParcelableArrayList(STATE_SPOTIFY_ARTIST_LIST);
            lastSelectedItemPosition = savedInstanceState.getInt(STATE_LAST_SELECTED_POSITION);
            if(searchAdapter != null){
                searchAdapter.setArtists(artistList);
                searchAdapter.activateItemAtPosition(lastSelectedItemPosition);
            }
        }
    }

    @Override
    public void onItemClick(View view, SpotifyArtist artist, int position){
        if(itemClickListener != null){
            itemClickListener.onArtistSelected(artist);
        }

        lastSelectedItemPosition = position;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        if(activity instanceof OnSpotifyArtistSelectedListener){
            itemClickListener = (OnSpotifyArtistSelectedListener) activity;
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        itemClickListener = null;
    }

    public void setActivateOnItemClick(boolean activateOnItemClick){
        this.activateOnItemClick = activateOnItemClick;

        if(searchAdapter != null){
            searchAdapter.setChoiceMode(activateOnItemClick ? ItemListAdapter.CHOICEMODE_SINGLE : ItemListAdapter.CHOICEMODE_NONE);
        }
    }

    public interface OnSpotifyArtistSelectedListener {

        public void onArtistSelected(SpotifyArtist artist);
    }
}
