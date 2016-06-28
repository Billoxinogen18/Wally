package com.wally.wally.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wally.wally.R;
import com.wally.wally.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog Photo chooser.
 * Parent fragment or activity must implement {@link PhotoChooserListener}
 * </p>
 * Note that caller should check for read access permissions
 */
public class PhotoChooserDialogFragment extends DialogFragment implements View.OnClickListener {

    @SuppressWarnings("unused")
    public static final String TAG = PhotoChooserDialogFragment.class.getSimpleName();
    private static final int ACTION_REQUEST_EXTERNAL_GALLERY = 102;
    private static final int RC_PERMISSION_READ_EXTERNAL_STORAGE = 11;

    private ImagesRecyclerViewAdapter mAdapter;
    private String mExternalChosenImage;
    private boolean mFlagDismissDialog = false;

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
            Cursor c = MediaStore.Images.Media.query(getContext().getContentResolver(),
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

    public static PhotoChooserDialogFragment newInstance() {
        return new PhotoChooserDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dv = LayoutInflater.from(getContext())
                .inflate(R.layout.photo_chooser_dialog, null, false);

        dv.findViewById(R.id.btn_dismiss).setOnClickListener(this);
        RecyclerView recyclerView = (RecyclerView) dv.findViewById(R.id.recycler_view_images);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), getGridColumnCount()));
        mAdapter = new ImagesRecyclerViewAdapter();
        recyclerView.setAdapter(mAdapter);
        loadContent();

        builder.setView(dv);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        setCancelable(false);
        return dialog;
    }

    private void loadContent() {
        if (Utils.checkExternalStorageReadPermission(getContext())) {
            mLoadImageData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    RC_PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (Utils.checkExternalStorageReadPermission(getContext())) {
                loadContent();
            } else {
                mFlagDismissDialog = true;
            }
        }
    }

    private int getGridColumnCount() {
        // This is optimal quantity based on rotation.
        return (int) (Utils.getScreenWidthDpi(getContext()) / 170);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_REQUEST_EXTERNAL_GALLERY) {
            if (resultCode == Activity.RESULT_OK && data.getData() != null) {
                mExternalChosenImage = data.getData().toString();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mExternalChosenImage)) {
            finishWithResult(mExternalChosenImage);
        }

        if (mFlagDismissDialog) {
            mFlagDismissDialog = false;
            finishWithResult(null);
        }
    }

    private void startExternalGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        Intent chooser = Intent.createChooser(intent, getString(R.string.title_activity_choose_photo));
        startActivityForResult(chooser, ACTION_REQUEST_EXTERNAL_GALLERY);
    }

    private void finishWithResult(String path) {
        PhotoChooserListener listener;
        if (getParentFragment() instanceof PhotoChooserListener) {
            listener = (PhotoChooserListener) getParentFragment();
        } else if (getActivity() instanceof PhotoChooserListener) {
            listener = (PhotoChooserListener) getActivity();
        } else {
            throw new IllegalStateException("No activity or parent fragment were Photo chooser listeners");
        }
        listener.onPhotoChosen(path);
        dismiss();
    }

    // Dismiss button
    @Override
    public void onClick(View v) {
        finishWithResult(null);
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
            View v = LayoutInflater.from(getContext()).inflate(R.layout.gallery_item, null);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            holder.imageView.setImageDrawable(null);
            holder.imageView.setBackground(null);
            if (position == 0) {
//                holder.imageView.setBackgroundResource(R.drawable.background_frame);
                holder.imageView.setImageResource(R.drawable.ic_external_gallery);
                holder.dateView.setVisibility(View.INVISIBLE);
            } else {
                position -= 1;
                ImageData data = mData.get(position);
                Glide.with(getContext())
                        .load(data.path)
                        .centerCrop()
                        .into(holder.imageView);

                holder.dateView.setText(Utils.formatDateSmart(getContext(), data.date));
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
                imageView = (ImageView) itemView.findViewById(R.id.iv_note_image);
                dateView = (TextView) itemView.findViewById(R.id.date_view);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (getAdapterPosition() == 0) {
                    startExternalGallery();
                } else {
                    ImageData imageData = mData.get(getAdapterPosition() - 1);
                    finishWithResult(imageData.path);
                }
            }
        }
    }
}
