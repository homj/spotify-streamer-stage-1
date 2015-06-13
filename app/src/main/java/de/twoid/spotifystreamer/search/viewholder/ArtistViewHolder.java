package de.twoid.spotifystreamer.search.viewholder;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.twoid.spotifystreamer.artist.ArtistActivity;
import de.twoid.spotifystreamer.ItemListAdapter.ItemViewHolder;
import de.twoid.spotifystreamer.R;
import de.twoid.spotifystreamer.object.SpotifyArtist;
import de.twoid.spotifystreamer.object.SpotifyImage;
import de.twoid.spotifystreamer.search.OnChildClickListener;

/**
 * Created by Johannes on 31.05.2015.
 */
public class ArtistViewHolder extends ItemViewHolder<SpotifyArtist> {

    private ImageView ivPicture;
    private TextView tvName;

    public ArtistViewHolder(ViewGroup parent){
        super(R.layout.item_artist, parent);

        ivPicture = (ImageView) itemView.findViewById(R.id.iv_picture);
        tvName = (TextView) itemView.findViewById(R.id.tv_name);
    }

    @Override
    public void bind(final SpotifyArtist artist, boolean isSelected, final OnChildClickListener<SpotifyArtist> clickListener){
        if(artist != null){
            tvName.setText(artist.name);

            Picasso.with(itemView.getContext())
                    .load(getImageUrl(artist.images))
                    .resizeDimen(R.dimen.artist_image_size, R.dimen.artist_image_size)
                    .centerCrop()
                    .placeholder(R.drawable.ic_artist_placeholder)
                    .into(ivPicture);

            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v){
                    clickListener.onChildClick(v, artist);
//                    Intent intent = new Intent(v.getContext(), ArtistActivity.class);
//                    intent.putExtra(ArtistActivity.EXTRA_SPOTIFY_ARTIST, artist);
//                    v.getContext().startActivity(intent);
                }
            });
            itemView.setSelected(isSelected);
        }else{
            tvName.setText("?");
            applyPlaceHolderImage();

            itemView.setOnClickListener(null);
            itemView.setActivated(false);
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
