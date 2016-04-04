package com.wally.wally;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;

import java.text.DateFormat;

/**
 * Created by ioane5 on 3/29/16.
 */
public final class Utils {


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
}
