package com.wally.wally.controllers.contentCreator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wally.wally.R;
import com.wally.wally.Utils;
import com.wally.wally.components.RevealPopup;

import java.util.ArrayList;
import java.util.List;

/**
 * This is popup showing photos
 * Created by ioane5 on 7/25/16.
 */
public class PhotoChooserPopup extends RevealPopup {

    private Context mContext;
    private PhotoChooserListener mListener;
    private ImagesRecyclerViewAdapter mAdapter;
    private AsyncTask<Void, Void, List<ImageData>> mLoadImageData = new AsyncTask<Void, Void, List<ImageData>>() {

        @Override
        protected List<ImageData> doInBackground(Void... params) {
            List<ImageData> allImages = new ArrayList<>();
//            allImages.addAll(getImagePaths(MediaStore.Images.Media.INTERNAL_CONTENT_URI));
            allImages.addAll(getImagePaths(MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            return allImages;
        }

        private ArrayList<ImageData> getImagePaths(Uri uri) {
            final String[] projection = new String[]{
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                    MediaStore.Images.ImageColumns.DATA};

            ArrayList<ImageData> images = new ArrayList<>();
            Cursor c = MediaStore.Images.Media.query(mContext.getContentResolver(),
                    uri, projection, null, MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC");
            if (c != null) {
                while (c.moveToNext()) {
                    // convert unix style date to java.
                    images.add(new ImageData("file://" + c.getString(1), c.getLong(0) * 1000));
                }
                c.close();
            }
            return images;
        }

        @Override
        protected void onPostExecute(List<ImageData> imageList) {
            super.onPostExecute(imageList);
            mAdapter.setData(imageList);
        }
    };

    public void show(View anchor, PhotoChooserListener listener) {
        setUp(anchor, R.layout.photo_chooser_popup);
        mContext = anchor.getContext();
        mListener = listener;

        RecyclerView recyclerView = (RecyclerView) mContentLayout.findViewById(R.id.recycler_view_images);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, getGridColumnCount()));
        mAdapter = new ImagesRecyclerViewAdapter();
        recyclerView.setAdapter(mAdapter);
        loadContent();
    }

    private void loadContent() {
        if (Utils.checkHasExternalStorageReadWritePermission(mContext)) {
            mLoadImageData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private int getGridColumnCount() {
        // This is optimal quantity based on rotation.
        return (int) (Utils.getScreenWidthDpi(mContext) / 170);
    }


    @Override
    protected void onDismiss() {

    }

    /**
     * Interface for getting result data
     */
    public interface PhotoChooserListener {
        void onPhotoChosen(String uri);
    }

    public static class ImageData {
        public String path;
        public long date;

        public ImageData(String path, long date) {
            this.path = path;
            this.date = date;
        }

        @Override
        public String toString() {
            return "ImageData{" +
                    "path='" + path + '\'' +
                    ", date=" + date +
                    '}';
        }
    }

    public class ImagesRecyclerViewAdapter extends RecyclerView.Adapter<ImagesRecyclerViewAdapter.VH> {
        private List<ImageData> mData;

        @SuppressLint("InflateParams")
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.gallery_item, null);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            holder.imageView.setImageDrawable(null);
            holder.imageView.setBackground(null);
            ImageData data = mData.get(position);
            Glide.with(mContext)
                    .load(data.path)
                    .centerCrop()
                    .into(holder.imageView);

            holder.dateView.setText(Utils.formatDateSmart(mContext, data.date));
            holder.dateView.setVisibility(View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            // +1 for first external library item
            return mData == null ? 0 : mData.size();
        }

        public void setData(List<ImageData> newData) {
            mData = newData;
            notifyDataSetChanged();
        }

        public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView imageView;
            TextView dateView;

            public VH(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.iv_note_image);
                dateView = (TextView) itemView.findViewById(R.id.date_view);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                ImageData imageData = mData.get(getAdapterPosition());
                mListener.onPhotoChosen(imageData.path);
                dismissPopup();
            }
        }
    }
}
