package de.twoid.spotifystreamer;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.twoid.spotifystreamer.ItemListAdapter.ItemViewHolder;

/**
 * Created by Johannes on 31.05.2015.
 */
public abstract class ItemListAdapter<I, V extends ItemViewHolder<I>> extends RecyclerView.Adapter<V> {

    protected static final int VIEWTYPE_INVALID = -1;
    protected List<I> itemList;

    public ItemListAdapter(){

    }

    public ItemListAdapter(List<I> itemList){
        this.itemList = itemList;
    }

    public void setItemList(List<I> itemList){
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(V itemViewHolder, int position){
        if(itemViewHolder != null && isValidPosition(position)){
            itemViewHolder.bind(itemList.get(position));
        }
    }

    @Override
    public int getItemCount(){
        return (itemList == null || itemList.isEmpty()) ? 0 : itemList.size();
    }

    protected boolean isValidPosition(int position){
        return position >= 0 && position < itemList.size();
    }

    public static abstract class ItemViewHolder<I> extends RecyclerView.ViewHolder {

        public ItemViewHolder(@LayoutRes int layoutResId, ViewGroup parent){
            super(inflate(layoutResId, parent));
        }

        private static View inflate(@LayoutRes int layoutResId, ViewGroup parent){
            if(parent == null){
                return null;
            }

            return LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        }

        public abstract void bind(I item);
    }
}
