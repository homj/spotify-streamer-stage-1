package de.twoid.spotifystreamer.artist;

import android.view.ViewGroup;

import java.util.List;

import de.twoid.spotifystreamer.ItemListAdapter;
import de.twoid.spotifystreamer.artist.viewholder.TrackViewHolder;
import de.twoid.spotifystreamer.object.SpotifyTrack;

/**
 * Created by Johannes on 31.05.2015.
 */
public class TrackAdapter extends ItemListAdapter<SpotifyTrack, TrackViewHolder> {

    private static final int VIEWTYPE_TRACK = 0;

    public TrackAdapter(){
        setHasStableIds(true);
    }

    public TrackAdapter(List<SpotifyTrack> itemList){
        super(itemList);
    }

    public void setTracks(List<SpotifyTrack> itemList){
        setItemList(itemList);
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        switch(viewType){
            case VIEWTYPE_TRACK:
                return new TrackViewHolder(viewGroup);
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position){
        if(!isValidPosition(position)){
            return VIEWTYPE_INVALID;
        }
        return VIEWTYPE_TRACK;
    }

    @Override
    public long getItemId(int position){
        if(!isValidPosition(position)){
            return 0;
        }

        return itemList.get(position).id.hashCode();
    }
}
