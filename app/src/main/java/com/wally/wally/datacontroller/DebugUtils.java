package com.wally.wally.datacontroller;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;

public class DebugUtils {
    public static final int CONTENT_PRE_PAGE = 5;
    public static final Id DEBUG_USER_ID =
            new Id(Id.PROVIDER_FIREBASE, "bPwMCPf2MWbebkLQrUuXKw3kYjW2");
    public static final User DEBUG_USER = new User(DEBUG_USER_ID.getId()).withGgId("");
    private static final String TAG = DebugUtils.class.getSimpleName();
    private static SecureRandom random = new SecureRandom();

    private static LatLng OFFICE_LAT_LNG = new LatLng(41.8057582f, 44.7681694f);

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
                        new LatLng(
                                random.nextDouble(),
                                random.nextDouble()
                        )
                ).withVisibility(
                        new Visibility()
                                .withTimeVisibility(null)
                                .withSocialVisibility(randomPublicity())
                                .withVisiblePreview(random.nextBoolean())
                                .withAnonymousAuthor(random.nextBoolean())
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

    public static void generatePublicEnumeratedRandomContents(int n, DataController controller) {
        for (int i = 0; i < n; i++) {
            Content content = generateRandomContent().withTitle("" + i);
            content.getVisibility().withSocialVisibility(new Visibility.SocialVisibility(Visibility.SocialVisibility.PUBLIC));
            controller.save(content);
        }
    }

    public static void generateRandomContents(DataController controller) {
        generateRandomContents(100, controller);
    }

    public static void generateRandomContentsNearOffice(DataController controller) {
        for (int i = 0; i < 50; i++) {
            controller.save(generateRandomContent()
                    .withImageUri(null)
                    .withLocation(randomLatLngNearPoint(OFFICE_LAT_LNG)));
        }
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

    private static LatLng randomLatLngNearPoint(LatLng point) {
        return new LatLng(
                point.latitude + nextSign() * (random.nextDouble() % 100) / 500,
                point.longitude + nextSign() * (random.nextDouble() % 100) / 500
        );
    }

    private static int nextSign() {
        return random.nextBoolean() ? -1 : 1;
    }

    // Warning: Does not work as u expect
    public static String randomStr(int length) {
        return new BigInteger(130, random).toString(32).substring(0, length);
    }

    public static FetchResultCallback debugCallback(final String tag) {
        return new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                Log.d(tag, "<" + result.size() + ">");
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
//        Log.d(tag, c.getTitle());

//        double diff = GeoUtils.distance(new LatLng(0,0), c.getLocation());
//        LatLng l = c.getLocation();
//        Log.d(tag, new GeoHash(l.latitude, l.longitude).getGeoHashString() + ": " + diff);

    }

    public static FetchResultCallback debugCallback() {
        return debugCallback(DataController.TAG);
    }


    public static FetchResultCallback fetchNextDebugCallback(final int count,
                                                             final ContentFetcher fetcher,
                                                             final FetchResultCallback callback) {
        return new FetchResultCallback() {

            @Override
            public void onResult(Collection<Content> result) {
                DebugUtils.debugCallback().onResult(result);
                fetcher.fetchNext(count, callback);
            }

            @Override
            public void onError(Exception e) {}
        };
    }

    public static FetchResultCallback fetchPrevDebugCallback(final int count,
                                                             final ContentFetcher fetcher,
                                                             final FetchResultCallback callback) {
        return new FetchResultCallback() {

            @Override
            public void onResult(Collection<Content> result) {
                DebugUtils.debugCallback().onResult(result);
                fetcher.fetchPrev(count, callback);
            }

            @Override
            public void onError(Exception e) {}
        };
    }

    public static void sanityCheck() {
//        contents.removeValue();
//        DebugUtils.generateRandomContents(100, this);
//        final ContentFetcher fetcher = createPublicContentFetcher();
//        int count = 15;
//        fetcher.fetchPrev(count,
//                fetchNextDebugCallback(count, fetcher,
//                        fetchNextDebugCallback(count, fetcher,
//                                fetchNextDebugCallback(count, fetcher,
//                                        DebugUtils.debugCallback()))));
    }

}