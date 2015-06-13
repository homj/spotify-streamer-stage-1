package de.twoid.spotifystreamer;

import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.twoid.spotifystreamer.ItemListAdapter.ItemViewHolder;
import de.twoid.spotifystreamer.search.OnChildClickListener;
import de.twoid.spotifystreamer.search.OnItemClickListener;

/**
 * Created by Johannes on 31.05.2015.
 */
public abstract class ItemListAdapter<I, V extends ItemViewHolder<I>> extends RecyclerView.Adapter<V> implements OnChildClickListener<I> {

    protected static final int VIEWTYPE_INVALID = -1;
    public static final int CHOICEMODE_NONE = 0;
    public static final int CHOICEMODE_SINGLE = 1;

    @IntDef({CHOICEMODE_NONE, CHOICEMODE_SINGLE})
    public @interface ChoiceMode {

    }

    protected List<I> itemList;
    @ChoiceMode
    private int choiceMode;
    private int lastSelectedPosition = -1;
    private OnItemClickListener<I> onItemClickListener;

    public ItemListAdapter(){

    }

    public ItemListAdapter(List<I> itemList){
        this.itemList = itemList;
    }

    public ItemListAdapter(List<I> itemList, int lastSelectedPosition){
        this.itemList = itemList;
        this.lastSelectedPosition = lastSelectedPosition;
    }

    public ItemListAdapter(List<I> itemList, int lastSelectedPosition, OnItemClickListener<I> onItemClickListener){
        this.itemList = itemList;
        this.lastSelectedPosition = lastSelectedPosition;
        this.onItemClickListener = onItemClickListener;
    }

    public void setItemList(List<I> itemList){
        if(this.itemList == itemList){
            return;
        }

        this.itemList = itemList;
        lastSelectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<I> onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public void setChoiceMode(@ChoiceMode int choiceMode){
        if(this.choiceMode == choiceMode){
            return;
        }

        this.choiceMode = choiceMode;

        if(choiceMode == CHOICEMODE_NONE && lastSelectedPosition >= 0){
            int oldSelectedPosition = lastSelectedPosition;
            lastSelectedPosition = -1;
            notifyItemChanged(oldSelectedPosition);
        }else if(choiceMode == CHOICEMODE_SINGLE){
            if(lastSelectedPosition >= 0){
                activateItemAtPosition(lastSelectedPosition);
            }
        }
    }

    public int getLastSelectedItemPosition(){
        return lastSelectedPosition;
    }

    @Override
    public void onBindViewHolder(V itemViewHolder, int position){
        if(itemViewHolder != null && isValidPosition(position)){
            itemViewHolder.bind(itemList.get(position), choiceMode != CHOICEMODE_NONE && position == lastSelectedPosition, this);
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

        public abstract void bind(I item, boolean isSelected, OnChildClickListener<I> clickListener);
    }

    @Override
    public void onChildClick(View view, I item){
        int position = itemList.indexOf(item);
        if(onItemClickListener != null){
            onItemClickListener.onItemClick(view, item, position);
        }

        if(choiceMode == CHOICEMODE_SINGLE){
            activateItemAtPosition(position);
        }
    }

    public void activateItemAtPosition(int position){
        int oldSelectedPosition = lastSelectedPosition;
        lastSelectedPosition = position;

        if(oldSelectedPosition >= 0){
            notifyItemChanged(oldSelectedPosition);
        }

        if(oldSelectedPosition != lastSelectedPosition && lastSelectedPosition >= 0){
            notifyItemChanged(lastSelectedPosition);
        }
    }
}
