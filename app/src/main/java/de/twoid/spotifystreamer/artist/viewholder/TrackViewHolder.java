package de.twoid.spotifystreamer.artist.viewholder;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.twoid.spotifystreamer.ItemListAdapter.ItemViewHolder;
import de.twoid.spotifystreamer.player.PlayerActivity;
import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.SpotifyImage;
import de.twoid.spotifystreamer.object.SpotifyTrack;
import de.twoid.spotifystreamer.search.OnChildClickListener;

/**
 * Created by Johannes on 31.05.2015.
 */
public class TrackViewHolder extends ItemViewHolder<SpotifyTrack> {

    private ImageView ivPicture;
    private TextView tvName;
    private TextView tvAlbum;

    public TrackViewHolder(ViewGroup parent){
        super(R.layout.item_track, parent);

        ivPicture = (ImageView) itemView.findViewById(R.id.iv_picture);
        tvName = (TextView) itemView.findViewById(R.id.tv_name);
        tvAlbum = (TextView) itemView.findViewById(R.id.tv_album);
    }

    @Override
    public void bind(final SpotifyTrack track, boolean isSelected, final OnChildClickListener<SpotifyTrack> clickListener){
        if(track != null){
            tvName.setText(track.name);

            if(track.album != null){
                tvAlbum.setVisibility(View.VISIBLE);
                tvAlbum.setText(track.album.name);

                Picasso.with(itemView.getContext())
                        .load(getImageUrl(track.album.images))
                        .resizeDimen(R.dimen.artist_image_size, R.dimen.artist_image_size)
                        .centerCrop()
                        .placeholder(R.drawable.ic_artist_placeholder)
                        .into(ivPicture);
            }else{
                tvAlbum.setVisibility(View.INVISIBLE);
                applyPlaceHolderImage();
            }

            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v){
                    clickListener.onChildClick(v, track);
                }
            });
        }else{
            tvName.setText("?");
            tvAlbum.setVisibility(View.INVISIBLE);
            applyPlaceHolderImage();

            itemView.setOnClickListener(null);
        }
    }

    private void applyPlaceHolderImage(){
        Picasso.with(itemView.getContext())
                .load(R.drawable.ic_artist_placeholder)
                .into(ivPicture);
    }

    private String getImageUrl(List<SpotifyImage> images){
        if(images == null || images.isEmpty()){
            return null;
        }

        if(images.size() == 1){
            return images.get(0).url;
        }

        SpotifyImage image;
        for(int i = 1; i < images.size(); i++){
            image = images.get(i);
            if(image.url != null && image.url.length() > 0){
                return image.url;
            }
        }

        return null;
    }
}
