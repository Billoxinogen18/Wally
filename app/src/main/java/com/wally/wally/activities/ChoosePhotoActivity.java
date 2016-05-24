package com.wally.wally.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wally.wally.R;
import com.wally.wally.Utils;

import java.util.ArrayList;
import java.util.List;

public class ChoosePhotoActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    public static final String TAG = ChoosePhotoActivity.class.getSimpleName();
    private static final int REQUEST_READ_PERMISSION = 121;
    private static final int ACTION_REQUEST_EXTERNAL_GALLERY = 102;
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
            if (imageList == null || imageList.size() == 0) {
                startExternalGallery();
            } else {
                mAdapter.setData(imageList);
            }
        }
    };

    public static Intent newIntent(Context context) {
        return new Intent(context, ChoosePhotoActivity.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_photo);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_images);
        recyclerView.setLayoutManager(new GridLayoutManager(this, getGridColumnCount()));
        mAdapter = new ImagesRecyclerViewAdapter();
        recyclerView.setAdapter(mAdapter);

        if (!Utils.checkExternalStorageReadPermission(getBaseContext())) {
            ActivityCompat.requestPermissions(ChoosePhotoActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSION);
        } else {
            mLoadImageData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private int getGridColumnCount() {
        // This is optimal quantity based on rotation.
        return (int) (Utils.getScreenWidthDpi(this) / 170);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_REQUEST_EXTERNAL_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLoadImageData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                finish();
                Toast.makeText(this, R.string.error_gallery_storage_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startExternalGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        Intent chooser = Intent.createChooser(intent, getString(R.string.title_activity_choose_photo));
        startActivityForResult(chooser, ACTION_REQUEST_EXTERNAL_GALLERY);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            View v = getLayoutInflater().inflate(R.layout.gallery_item, null);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            holder.imageView.setImageDrawable(null);
            holder.imageView.setBackground(null);
            if (position == 0) {
                holder.imageView.setBackgroundResource(R.drawable.background_frame);
                holder.imageView.setImageResource(R.drawable.ic_external_gallery);
                holder.dateView.setVisibility(View.INVISIBLE);
            } else {
                position -= 1;
                ImageData data = mData.get(position);
                Glide.with(getBaseContext())
                        .load(data.path)
                        .centerCrop()
                        .into(holder.imageView);

                holder.dateView.setText(Utils.formatDateSmart(getBaseContext(), data.date));
                holder.dateView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            // +1 for first external library item
            return mData == null ? 0 : mData.size() + 1;
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
                imageView = (ImageView) itemView.findViewById(R.id.image_view);
                dateView = (TextView) itemView.findViewById(R.id.date_view);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (getAdapterPosition() == 0) {
                    startExternalGallery();
                } else {
                    ImageData imageData = mData.get(getAdapterPosition() - 1);
                    Intent result = new Intent();
                    result.setData(Uri.parse(imageData.path));
                    setResult(RESULT_OK, result);
                    finish();
                }
            }
        }
    }
}
