package com.wally.wally.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wally.wally.R;
import com.wally.wally.Utils;

import java.util.ArrayList;
import java.util.List;

public class ChoosePhotoActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    public static final String TAG = ChoosePhotoActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ImagesRecyclerViewAdapter mAdapter;
    private AsyncTask<Void, Void, List<ImageData>> mLoadImages = new AsyncTask<Void, Void, List<ImageData>>() {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO show progress.
        }

        @Override
        protected List<ImageData> doInBackground(Void... params) {
            List<ImageData> allImages = new ArrayList<>();
            allImages.addAll(getImagePaths(MediaStore.Images.Media.INTERNAL_CONTENT_URI));
            allImages.addAll(getImagePaths(MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            return allImages;
        }

        private ArrayList<ImageData> getImagePaths(Uri uri) {
            final String[] projection = new String[]{
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                    MediaStore.Images.ImageColumns.DATA};

            ArrayList<ImageData> images = new ArrayList<>();
            Cursor c = MediaStore.Images.Media.query(getContentResolver(),
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
            // TODO stop progress.
            if (imageList == null || imageList.size() == 0) {
                // TODO show empty view
            } else {
                mAdapter.setData(imageList);
            }
        }
    };

    public static Intent newIntent(Activity from) {
        return new Intent(from, ChoosePhotoActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_photo);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_images);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, getGridColumnCount()));
        mAdapter = new ImagesRecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mLoadImages.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private int getGridColumnCount() {
        // This is optimal quantity based on rotation.
        return (int) (Utils.getScreenWidthDpi(this) / 170);
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

        public ImagesRecyclerViewAdapter() {

        }

        @SuppressLint("InflateParams")
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.gallery_item, null);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            ImageData data = mData.get(position);

            holder.imageView.setImageDrawable(null);
            Glide.with(getBaseContext())
                    .load(data.path)
                    .centerCrop()
                    .into(holder.imageView);

            holder.dateView.setText(Utils.formatDateSmart(getBaseContext(), data.date));
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public void setData(List<ImageData> newData) {
            mData = newData;
            notifyDataSetChanged();
            // TODO maybe better notifyItemInserted();
        }

        public class VH extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView dateView;

            public VH(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.image_view);
                dateView = (TextView) itemView.findViewById(R.id.date_view);
            }
        }
    }
}
