package de.twoid.spotifystreamer.search;

import android.view.View;

import de.twoid.spotifystreamer.object.SpotifyArtist;

/**
 * Created by Johannes on 02.06.2015.
 */
public interface OnItemClickListener<I> {
    public void onItemClick(View view, I item, int position);
}
