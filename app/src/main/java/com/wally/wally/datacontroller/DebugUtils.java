package com.wally.wally.datacontroller;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
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
    public static final String TAG = DebugUtils.class.getSimpleName();
    public static final String[] USER_IDS = new String[] {
            "bPwMCPf2MWbebkLQrUuXKw3kYjW2", // Johan's ID
            "8g7t26liJZgP6Z7jHgTkTdZLk632", // Georg's ID
            "8g7t26liJZgP6Z7jHgTkTdZLk632", // Tango's ID
            "uSlLJUtZqbRDTMeLU4MdcToS8ZZ2", // Misha's ID
    };

    public static final Id DEBUG_USER_ID =
            new Id(Id.PROVIDER_FIREBASE, "bPwMCPf2MWbebkLQrUuXKw3kYjW2");
    public static final User DEBUG_USER = new User(DEBUG_USER_ID.getId()).withGgId("");

    public static final LatLng OFFICE_LAT_LNG = new LatLng(41.8057582f, 44.7681694f);

    public static String[] ROOMS = new String[] {
            "a", "b", "c", "d", "e",
            "f", "g", "h", "i", "j",
            "k", "l", "m", "n", "o"
    };

    private static DataController datacontroller;
    private static SecureRandom random = new SecureRandom();

    // This method is for debugging purposes
    public static Content generateRandomContent() {
        return new Content()
                .withUuid(randomStr(1))
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
                                .withAnonymousAuthor(false)
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
            content.getVisibility().withSocialVisibility(Visibility.PUBLIC);
            controller.save(content);
        }
    }

    public static void generateRandomContentsNearOffice(DataController controller) {
        for (int i = 0; i < 60; i++) {
            controller.save(generateRandomContent()
                    .withImageUri(null)
                    .withLocation(randomLatLngNearPoint(OFFICE_LAT_LNG)));
        }
    }

    private static String randomRoom() {
        return ROOMS[random.nextInt(ROOMS.length)];
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

    public static void refreshContents(DatabaseReference contents, DataController datacontroller) {
        contents.removeValue();
        generateRandomContents(100, datacontroller);
    }

    public static void sanityCheck(DataController datacontroller) {
        DebugUtils.datacontroller = datacontroller;
//        final ContentFetcher fetcher = datacontroller.createPublicContentFetcher(new LatLng(0, 0), 100);
//        final ContentFetcher fetcher = datacontroller.createPublicContentFetcher();
//        int count = 15;
//        fetcher.fetchPrev(count,
//                fetchNextDebugCallback(count, fetcher,
//                        fetchNextDebugCallback(count, fetcher,
//                                fetchNextDebugCallback(count, fetcher,
//                                        fetchPrevDebugCallback(count, fetcher,
//                                            debugCallback())))));
    }


}