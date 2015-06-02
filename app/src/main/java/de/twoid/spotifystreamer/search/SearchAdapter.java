package de.twoid.spotifystreamer.search;

import android.view.ViewGroup;

import java.util.List;

import de.twoid.spotifystreamer.ItemListAdapter;
import de.twoid.spotifystreamer.object.SpotifyArtist;
import de.twoid.spotifystreamer.search.viewholder.ArtistViewHolder;

/**
 * Created by Johannes on 31.05.2015.
 */
public class SearchAdapter extends ItemListAdapter<SpotifyArtist, ArtistViewHolder> {

    private static final int VIEWTYPE_ARTIST = 0;

    public SearchAdapter(){
        setHasStableIds(true);
    }

    public SearchAdapter(List<SpotifyArtist> itemList){
        super(itemList);
    }

    public void setArtists(List<SpotifyArtist> itemList){
        setItemList(itemList);
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        switch(viewType){
            case VIEWTYPE_ARTIST:
                return new ArtistViewHolder(viewGroup);
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position){
        if(!isValidPosition(position)){
            return VIEWTYPE_INVALID;
        }
        return VIEWTYPE_ARTIST;
    }

    @Override
    public long getItemId(int position){
        if(!isValidPosition(position)){
            return 0;
        }

        return itemList.get(position).id.hashCode();
    }
}
