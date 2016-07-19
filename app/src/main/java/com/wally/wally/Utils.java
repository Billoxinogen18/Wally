package com.wally.wally;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.atap.tangoservice.Tango;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.wally.wally.datacontroller.adf.AdfSyncInfo;
import com.wally.wally.datacontroller.callbacks.Callback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.utils.SerializableLatLng;
import com.wally.wally.userManager.SocialUser;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Utility functions which are not specific to one part of the code goes here.
 * <p/>
 * Created by ioane5 on 3/29/16.
 */
public final class Utils {


    @SuppressWarnings("unused")
    private static final String TAG = Utils.class.getSimpleName();

    /**
     * Hides keyboard from input view. (note that keyboard must be focused on that view)
     *
     * @param input   view on which keyboard is open.
     * @param context context to get the keyboard manager.
     */
    public static void hideSoftKeyboard(View input, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

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
            return DateUtils.getRelativeTimeSpanString(date, now, 0L, DateUtils.FORMAT_ABBREV_ALL).toString();
        } else {
            return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_ABBREV_RELATIVE);
        }
    }

    public static boolean hasADFPermissions(Context context) {
        return Tango.hasPermission(context, Tango.PERMISSIONTYPE_ADF_LOAD_SAVE);
    }

    public static Bitmap createBitmapFromContent(Content content) {
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
        View root = cv.findViewById(R.id.root);

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

        if (content.getColor() != null) {
            root.setBackgroundColor(content.getColor());
        }

        Resources res = context.getResources();
        cv.measure(
                View.MeasureSpec.makeMeasureSpec(res.getDimensionPixelSize(R.dimen.wall_content_max_width), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        cv.layout(0, 0, cv.getMeasuredWidth(), cv.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(cv.getMeasuredWidth(),
                cv.getMeasuredHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        cv.draw(canvas);
        return bitmap;
    }


    public static boolean isTangoDevice(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.projecttango.tango", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static
    @Nullable
    Palette.Swatch getMostPopulousSwatch(Palette palette) {
        Palette.Swatch mostPopulous = null;
        if (palette != null) {
            for (Palette.Swatch swatch : palette.getSwatches()) {
                if (mostPopulous == null || swatch.getPopulation() > mostPopulous.getPopulation()) {
                    mostPopulous = swatch;
                }
            }
        }
        return mostPopulous;
    }


    /**
     * Calculate a variant of the color to make it more suitable for overlaying information. Light
     * colors will be lightened and dark colors will be darkened
     *
     * @param color               the color to adjust
     * @param isDark              whether {@code color} is light or dark
     * @param lightnessMultiplier the amount to modify the color e.g. 0.1f will alter it by 10%
     * @return the adjusted color
     */
    public static
    @ColorInt
    int scrimify(@ColorInt int color,
                 boolean isDark,
                 @FloatRange(from = 0f, to = 1f) float lightnessMultiplier) {
        float[] hsl = new float[3];
        android.support.v4.graphics.ColorUtils.colorToHSL(color, hsl);

        if (!isDark) {
            lightnessMultiplier += 1f;
        } else {
            lightnessMultiplier = 1f - lightnessMultiplier;
        }


        hsl[2] = Math.max(0f, Math.min(1f, hsl[2] * lightnessMultiplier));
        return android.support.v4.graphics.ColorUtils.HSLToColor(hsl);
    }

    public static
    @ColorInt
    int modifyAlpha(@ColorInt int color,
                    @IntRange(from = 0, to = 255) int alpha) {
        return (color & 0x00ffffff) | (alpha << 24);
    }

    /**
     * Change color of drawable
     */
    public static Drawable tintDrawable(Drawable drawable, int color) {
        Drawable mutated = drawable.mutate();
        mutated.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return mutated;
    }


    public static double getRadius(LatLng center, LatLng northEast) {
        SerializableLatLng c = SerializableLatLng.fromLatLng(center);
        SerializableLatLng ne = SerializableLatLng.fromLatLng(northEast);

        // r = radius of the earth in statute miles
        double r = 3963.0;

        // Convert lat or lng from decimal degrees into radians (divide by 57.2958)
        double lat1 = center.getLatitude() / 57.2958;
        double lon1 = center.getLongitude() / 57.2958;
        double lat2 = northEast.getLatitude() / 57.2958;
        double lon2 = northEast.getLongitude() / 57.2958;

        // distance = circle radius from center to Northeast corner of bounds
        return r * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
    }

    public static void throwError() {
        throw new RuntimeException("You did something you should not do! WTF dude?");
    }

    /**
     * @param userId user id to check
     * @return true if userId is same signed user.
     */
    public static boolean isCurrentUser(String userId) {
        SocialUser currentUser = App.getInstance().getUserManager().getUser();
        return currentUser != null &&
                TextUtils.equals(userId, currentUser.getBaseUser().getId().getId());
    }

    public static SerializableLatLng extractLatLng(@NonNull Location location) {
        return new SerializableLatLng(location.getLatitude(), location.getLongitude());
    }

    public static String getAdfFilesFolder() {
        String folder = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "Wally";
        File file = new File(folder);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }
        return folder;
    }

    public static String getAdfFilePath(String uuid) {
        return getAdfFilesFolder() + "/" + uuid;
    }

    /**
     * Sorts Adf list with respect to location.
     */
    public static void sortWithLocation(ArrayList<AdfSyncInfo> list, final SerializableLatLng location) {
        Collections.sort(list, new Comparator<AdfSyncInfo>() {
            @Override
            public int compare(AdfSyncInfo one, AdfSyncInfo two) {
                SerializableLatLng loc1 = one.getAdfMetaData().getLatLng();
                SerializableLatLng loc2 = two.getAdfMetaData().getLatLng();

                float[] result1 = new float[3];
                float[] result2 = new float[3];

                Location.distanceBetween(loc1.getLatitude(), loc1.getLongitude(), location.getLatitude() , location.getLongitude(), result1);
                Location.distanceBetween(loc2.getLatitude(), loc2.getLongitude(), location.getLatitude(), location.getLongitude(), result2);

                Float distance1 = result1[0];
                Float distance2 = result2[0];

                return distance1.compareTo(distance2);
            }
        });
    }


    @SuppressWarnings("MissingPermission")
    public static void getNewLocation(final GoogleApiClient googleApiClient, final Callback<SerializableLatLng> callback) {
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (location != null) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                    callback.onResult(Utils.extractLatLng(location));
                }
            }
        };

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
    }

    public static LatLng serializableLatLngToLatLng(SerializableLatLng location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
    public static SerializableLatLng latLngToSerializableLatLng(LatLng location) {
        return new SerializableLatLng(location.getLatitude(), location.getLongitude());
    }
}
