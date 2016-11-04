package com.wally.wally;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.atap.tangoservice.Tango;
import com.wally.wally.objects.content.Content;
import com.wally.wally.objects.content.SerializableLatLng;
import com.wally.wally.userManager.SocialUser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

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
     * Show Keyboard when View is focused.
     * Note that keyboard won't show if view isn't focused first!
     *
     * @param input   View that want's to show keyboard.
     * @param context context to get the keyboard manager.
     */
    public static void showKeyboard(View input, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
    }

    /**
     * Permission checking methods should start with 'check' and end with 'permission'
     * if not there will be lint error. (Lint won't understand that this is permission checking.)
     *
     * @param context to check permission.
     * @return true if we have location permission.
     */
    public static boolean checkHasLocationPermission(Context context) {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP ||
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if app has camera permission
     */
    public static boolean checkHasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @return true if we have External storage access permission.
     */
    public static boolean checkHasExternalStorageReadWritePermission(Context context) {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    public static Intent getAppSettingsIntent(Context context) {
        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return i;
    }

    public static float getScreenWidthDpi(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.density;

    }

    public static String formatDateSmart(Context context, long date) {
        long now = System.currentTimeMillis();
        if (DateUtils.isToday(date)) {
            return DateUtils.getRelativeDateTimeString(context, date, now, 0L, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        } else {
            return DateUtils.formatDateTime(context, date, DateUtils.FORMAT_ABBREV_RELATIVE);
        }
    }

    public static String formatDateSmartShort(Context context, long date) {
        long now = System.currentTimeMillis();
        if (DateUtils.isToday(date)) {
            return DateUtils.getRelativeTimeSpanString(date, now, 0L, DateUtils.FORMAT_ABBREV_RELATIVE).toString()
                    .replace("ago", "");
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
        if (content.getTextColor() != null) {
            titleTV.setTextColor(content.getTextColor());
            noteTV.setTextColor(content.getTextColor());
        }

        Resources res = context.getResources();
        cv.measure(
                View.MeasureSpec.makeMeasureSpec(res.getDimensionPixelSize(R.dimen.wall_content_max_width), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        cv.layout(0, 0, cv.getMeasuredWidth(), cv.getMeasuredHeight());

        return createBitmapFromView(cv);
    }

    public static Bitmap createBitmapFromView(View v) {
        int w = v.getMeasuredWidth();
        int h = v.getMeasuredHeight();

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        v.draw(canvas);
        return bitmap;
    }


    public static boolean isTangoDevice(Context context) {
        PackageManager pm = context.getPackageManager();
        return hasPackageInstalled("com.google.tango", pm) || hasPackageInstalled("com.projecttango.tango", pm);
    }

    public static boolean hasPackageInstalled(String packageName, PackageManager pm) {
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "isTangoDevice()" + e);
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
        // r = radius of the earth in statute miles
        double r = 3963.0;

        // Convert lat or lng from decimal degrees into radians (divide by 57.2958)
        double lat1 = center.latitude / 57.2958;
        double lon1 = center.longitude / 57.2958;
        double lat2 = northEast.latitude / 57.2958;
        double lon2 = northEast.longitude / 57.2958;

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
        SocialUser currentUser = App.getInstance().getSocialUserManager().getUser();
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

    @SuppressWarnings("MissingPermission")
    public static void getLocation(final GoogleApiClient googleApiClient, final Callback<SerializableLatLng> callback) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            callback.onResult(Utils.extractLatLng(location));
        } else {
            getNewLocation(googleApiClient, callback);
        }
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

    public static int dpToPx(Context context, int dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5);
    }

    public static LatLng serializableLatLngToLatLng(SerializableLatLng location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static SerializableLatLng latLngToSerializableLatLng(LatLng location) {
        return new SerializableLatLng(location.latitude, location.longitude);
    }


    /**
     * Tries to add circular reveal effect if possible.
     *
     * @param from       circular reveal from
     * @param targetView target view that will be revealed
     */
    public static void addCircularReveal(final View from, final View targetView, final boolean start, @Nullable final Callback<Void> animationEndCallback) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            float startRadius;
            float finalRadius;
            float hypot = (float) Math.hypot(targetView.getWidth(), targetView.getHeight());
            float radius = from.getWidth() / 3;
            if (start) {
                startRadius = radius;
                finalRadius = hypot;
            } else {
                startRadius = hypot;
                finalRadius = radius;
            }

            Rect center = new Rect();
            from.getGlobalVisibleRect(center);

            Animator anim = ViewAnimationUtils.createCircularReveal(
                    targetView,
                    (int) center.exactCenterX(),
                    center.top,
                    startRadius, finalRadius);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (animationEndCallback != null) {
                        animationEndCallback.onResult(null);
                    }
                }
            });
            anim.start();
        }
    }

    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void getAddressForLocation(final Context ctx, final SerializableLatLng latLng,
                                             final Callback<String> callback) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            latLng.getLatitude(), latLng.getLongitude(), 10);
                    Address address = addresses.get(0);

                    String city = address.getLocality();
                    String street = address.getFeatureName();
                    String country = address.getCountryName();

                    String addr = String.format("%s,%s (%s)", street, city, country);
                    if (address.getThoroughfare() != null) {
                        return address.getThoroughfare();
                    }
                    return addr;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (!TextUtils.isEmpty(s)) {
                    callback.onResult(s);
                } else {
                    callback.onError(null);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static String getAssetContentAsString(Context context, String filepath) {
        String content = "";
        try {
            InputStream is = context.getAssets().open(filepath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                content += line;
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            content = null;
        }
        return content;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    public interface Callback<T> {
        void onResult(T result);

        void onError(Exception e);
    }
}