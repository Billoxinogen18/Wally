package com.wally.wally;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.atap.tangoservice.Tango;
import com.wally.wally.datacontroller.content.Content;

import java.text.DateFormat;

/**
 * Utility functions which are not specific to one part of the code goes here.
 * <p/>
 * Created by ioane5 on 3/29/16.
 */
public final class Utils {


    @SuppressWarnings("unused")
    private static final String TAG = Utils.class.getSimpleName();

    /**
     * Permission checking methods should start with 'check' and end with 'permission'
     * if not there will be lint error. (Lint won't understand that this is permission checking.)
     *
     * @param context to check permission.
     * @return true if we have location permission.
     */
    public static boolean checkLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @return true if we have External storage access permission.
     */
    public static boolean checkExternalStorageReadPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static float getScreenWidthDpi(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.density;

    }

    public static String formatDateSmart(Context context, long date) {
        long now = System.currentTimeMillis();
        if (DateUtils.isToday(date)) {
            return DateUtils.formatSameDayTime(date, now, DateFormat.MEDIUM, DateFormat.MEDIUM).toString();
        } else {
            return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_ABBREV_ALL);
        }
    }

    public static boolean hasNoADFPermissions(Context context) {
        return !Tango.hasPermission(context, Tango.PERMISSIONTYPE_ADF_LOAD_SAVE);
    }

    public static Bitmap createBitmapFromContent(Content content){
        Context context = App.getContext();
        return createBitmapFromContent(content, context);
    }

    public static Bitmap createBitmapFromContent(Content content, Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context.setTheme(R.style.AppTheme);
        @SuppressLint("InflateParams") View cv = inflater.inflate(R.layout.wall_content, null, false);

        TextView titleTV = (TextView) cv.findViewById(R.id.tv_title);
        TextView noteTV = (TextView) cv.findViewById(R.id.tv_note);
        ImageView imageView = (ImageView) cv.findViewById(R.id.image);

        titleTV.setText(content.getTitle());
        noteTV.setText(content.getNote());

        titleTV.setVisibility(TextUtils.isEmpty(titleTV.getText()) ? View.GONE : View.VISIBLE);
        noteTV.setVisibility(TextUtils.isEmpty(noteTV.getText()) ? View.GONE : View.VISIBLE);

        try {
            Drawable image = null;
            if (!TextUtils.isEmpty(content.getImageUri())) {
                image = Glide.with(context)
                        .load(content.getImageUri())
                        .fitCenter()
                        .into(700, 700)
                        .get();
            }
            imageView.setVisibility(image == null ? View.GONE : View.VISIBLE);
            imageView.setImageDrawable(image);
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setVisibility(View.GONE);
        }

        Resources res = context.getResources();
        cv.measure(
                View.MeasureSpec.makeMeasureSpec(res.getDimensionPixelSize(R.dimen.wall_content_max_width), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        cv.layout(0, 0, cv.getMeasuredWidth(), cv.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(cv.getMeasuredWidth(),
                cv.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        cv.draw(canvas);
        return bitmap;
    }
}
