package de.twoid.spotifystreamer.artist.viewholder;

import android.view.ViewGroup;
import android.widget.TextView;

import de.twoid.spotifystreamer.ItemListAdapter.ItemViewHolder;
import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.search.OnChildClickListener;

/**
 * Created by Johannes on 03.06.2015.
 */
public class SubheadViewHolder extends ItemViewHolder<Integer> {

    private TextView tvSubhead;

    public SubheadViewHolder(ViewGroup parent){
        super(R.layout.item_subhead, parent);
        tvSubhead = (TextView) itemView.findViewById(R.id.tv_subhead);
    }

    @Override
    public void bind(Integer subheadTextResId, boolean isSelected, OnChildClickListener<Integer> clickListener){
        tvSubhead.setText(subheadTextResId);
    }
}
