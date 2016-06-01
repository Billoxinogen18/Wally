package com.wally.wally.components;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Meravici on 6/1/2016.
 */
public abstract class FilterRecyclerViewAdapter<ViewHolderT extends RecyclerView.ViewHolder, DataT> extends RecyclerView.Adapter<ViewHolderT> {
    private List<DataT> mData;
    private List<DataT> mBackedData;

    protected abstract List<DataT> filterData(String query);

    public List<DataT> getFilteredData(){
        return mData;
    }

    public List<DataT> getFullData(){
        return mBackedData;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setData(List<DataT> data) {
        mData = data;
        mBackedData = new ArrayList<>(data);
        // this thing adds insert animation and updates data
        notifyItemRangeInserted(0, getItemCount());
    }

    public void removeItem(DataT item) {
        mBackedData.remove(item);
        removeItem(mData.indexOf(item));
    }

    public void updateItem(DataT item) {
        int dataPos = mData.indexOf(item);
        int backedPos = mBackedData.indexOf(item);

        if (dataPos >= 0) {
            mData.set(dataPos, item);
            notifyItemChanged(dataPos);
        }
        if (backedPos >= 0) {
            mBackedData.set(backedPos, item);
        }
    }

    public void filter(@Nullable String query) {
        if (mBackedData == null) {
            return;
        }
        List<DataT> filtered = filterData(query);
        animateTo(filtered);
    }

    /**
     * Filter Logic
     **/
    private void animateTo(List<DataT> data) {
        applyAndAnimateRemovals(data);
        applyAndAnimateAdditions(data);
        applyAndAnimateMovedItems(data);

    }

    private void applyAndAnimateRemovals(List<DataT> newData) {
        for (int i = mData.size() - 1; i >= 0; i--) {
            DataT model = mData.get(i);
            if (!newData.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<DataT> newData) {
        for (int i = 0, count = newData.size(); i < count; i++) {
            DataT model = newData.get(i);
            if (!mData.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<DataT> newData) {
        for (int toPosition = newData.size() - 1; toPosition >= 0; toPosition--) {
            DataT model = newData.get(toPosition);
            final int fromPosition = mData.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private DataT removeItem(int position) {
        DataT model = mData.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, DataT model) {
        mData.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        DataT model = mData.remove(fromPosition);
        mData.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
}