package com.wally.wally.controllers.contentCreator.peopleChooser;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Meravici on 6/1/2016.
 */
public abstract class FilterRecyclerViewAdapter<ViewHolderT extends RecyclerView.ViewHolder, DataT> extends RecyclerView.Adapter<ViewHolderT> {
    private List<DataT> mFilteredData;
    private List<DataT> mData;

    protected abstract List<DataT> filterData(String query);

    public List<DataT> getFilteredData(){
        return mFilteredData;
    }

    public List<DataT> getFullData(){
        return mData;
    }

    @Override
    public int getItemCount() {
        return mFilteredData == null ? 0 : mFilteredData.size();
    }

    public void setData(List<DataT> data) {
        mFilteredData = data;
        mData = new ArrayList<>(data);
        // this thing adds insert animation and updates data
        notifyItemRangeInserted(0, getItemCount());
    }

    public void removeItem(DataT item) {
        mData.remove(item);
        removeItem(mFilteredData.indexOf(item));
    }

    public void updateItem(DataT item) {
        int dataPos = mFilteredData.indexOf(item);
        int backedPos = mData.indexOf(item);

        if (dataPos >= 0) {
            mFilteredData.set(dataPos, item);
            notifyItemChanged(dataPos);
        }
        if (backedPos >= 0) {
            mData.set(backedPos, item);
        }
    }

    public void filter(@Nullable String query) {
        if (mData == null) {
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
        for (int i = mFilteredData.size() - 1; i >= 0; i--) {
            DataT model = mFilteredData.get(i);
            if (!newData.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<DataT> newData) {
        for (int i = 0, count = newData.size(); i < count; i++) {
            DataT model = newData.get(i);
            if (!mFilteredData.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<DataT> newData) {
        for (int toPosition = newData.size() - 1; toPosition >= 0; toPosition--) {
            DataT model = newData.get(toPosition);
            final int fromPosition = mFilteredData.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private DataT removeItem(int position) {
        DataT model = mFilteredData.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, DataT model) {
        mFilteredData.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        DataT model = mFilteredData.remove(fromPosition);
        mFilteredData.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
}