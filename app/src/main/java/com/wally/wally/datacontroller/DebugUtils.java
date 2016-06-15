package com.wally.wally.datacontroller;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.firebase.geofire.GeoHash;
import com.wally.wally.datacontroller.firebase.geofire.GeoUtils;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;

public class DebugUtils {
    public static final int CONTENT_PRE_PAGE = 5;
    public static final Id DEBUG_USER_ID =
            new Id(Id.PROVIDER_FIREBASE, "uSlLJUtZqbRDTMeLU4MdcToS8ZZ2");
    public static final User DEBUG_USER = new User(DEBUG_USER_ID.getId()).withGgId("");
    private static SecureRandom random = new SecureRandom();

    // This method is for debugging purposes
    public static Content generateRandomContent() {
        return new Content()
                .withUuid(randomStr(5))
                .withNote(randomStr(15))
                .withTitle(randomStr(7))
                .withColor(random.nextInt())
                .withImageUri("http://" + randomStr(10))
                .withAuthorId(DEBUG_USER_ID.getId())
                .withLocation(
//                        new LatLng(
//                                random.nextInt(),
//                                random.nextInt()
//                        )
                        new LatLng(
                                random.nextDouble(),
                                random.nextDouble()
                        )
                ).withVisibility(
                        new Visibility()
                                .withTimeVisibility(null)
                                .withSocialVisibility(randomPublicity())
                                .withVisiblePreview(random.nextBoolean())
                ).withTangoData(
                        new TangoData()
                                .withScale((double) random.nextInt())
                                .withRotation(randomDoubleArray())
                                .withTranslation(randomDoubleArray()));
    }

    public static void generateRandomContents(int n, DataController controller) {
        for (int i = 0; i < n; i++) {
            controller.save(generateRandomContent());
        }
    }

    public static void generateRandomContents(DataController controller) {
        generateRandomContents(100, controller);
    }

    private static Visibility.SocialVisibility randomPublicity() {
        //noinspection WrongConstant
        return new Visibility.SocialVisibility(
                Math.abs(random.nextInt()) % Visibility.SocialVisibility.getSize());
    }

    private static Double[] randomDoubleArray() {
        return new Double[]{
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble()
        };
    }

    // Warning: Does not work as u expect
    public static String randomStr(int length) {
        return new BigInteger(130, random).toString(32).substring(0, length);
    }

    public static FetchResultCallback debugCallback(final String tag) {
        return new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                Log.d(tag, "" + result.size());
                for (Content c : result) {
                    logContent(c, tag);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(tag, "Shit gone wrong!");
            }
        };
    }

    private static void logContent(Content c, String tag) {
        Log.d(tag, c.getId());

//        double diff = GeoUtils.distance(new LatLng(0,0), c.getLocation());
//        LatLng l = c.getLocation();
//        Log.d(tag, new GeoHash(l.latitude, l.longitude).getGeoHashString() + ": " + diff);

    }

    public static FetchResultCallback debugCallback() {
        return debugCallback(DataController.TAG);
    }
}
