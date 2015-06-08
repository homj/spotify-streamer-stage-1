package de.twoid.spotifystreamer.artist;

import android.view.ViewGroup;

import java.util.List;

import de.twoid.spotifystreamer.ItemListAdapter;
import de.twoid.spotifystreamer.ItemListAdapter.ItemViewHolder;
import de.twoid.spotifystreamer.artist.viewholder.SubheadViewHolder;
import de.twoid.spotifystreamer.artist.viewholder.TrackViewHolder;
import de.twoid.spotifystreamer.object.SpotifyTrack;

/**
 * Created by Johannes on 31.05.2015.
 */
public class TrackAdapter extends ItemListAdapter<SpotifyTrack, ItemViewHolder<SpotifyTrack>> {

    private static final int VIEWTYPE_TRACK = 0;
    private static final int VIEWTYPE_SUBHEAD = 1;

    private Integer subheadTextResId;

    public TrackAdapter(int subheadTextResId){
        this.subheadTextResId = subheadTextResId;
        setHasStableIds(true);
    }

    public TrackAdapter(List<SpotifyTrack> itemList, int subheadTextResId){
        super(itemList);
        this.subheadTextResId = subheadTextResId;
        setHasStableIds(true);
    }

    public void setTracks(List<SpotifyTrack> itemList){
        setItemList(itemList);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        switch(viewType){
            case VIEWTYPE_TRACK:
                return new TrackViewHolder(viewGroup);
            case VIEWTYPE_SUBHEAD:
                return new SubheadViewHolder(viewGroup);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(ItemViewHolder itemViewHolder, int position){
        if(position == 0){
            ((SubheadViewHolder) itemViewHolder).bind(subheadTextResId, false, null);
        }

        super.onBindViewHolder(itemViewHolder, position - 1);
    }

    @Override
    public int getItemCount(){
        return super.getItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position){
        if(position == 0){
            return VIEWTYPE_SUBHEAD;
        }

        if(isValidPosition(position - 1)){
            return VIEWTYPE_TRACK;
        }

        return VIEWTYPE_INVALID;
    }

    @Override
    public long getItemId(int position){
        if(position == 0 || !isValidPosition(position - 1)){
            return 0;
        }

        return itemList.get(position - 1).id.hashCode();
    }
}
