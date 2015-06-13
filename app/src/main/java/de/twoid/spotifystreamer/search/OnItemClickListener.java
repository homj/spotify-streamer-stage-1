package de.twoid.spotifystreamer.search;

import android.view.View;

/**
 * Created by Johannes on 02.06.2015.
 */
public interface OnItemClickListener<I> {

    void onItemClick(View view, I item, int position);
}
