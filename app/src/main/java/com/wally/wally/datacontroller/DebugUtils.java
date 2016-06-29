package com.wally.wally.datacontroller;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.wally.wally.datacontroller.adf.AdfMetaData;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.fetchers.ContentFetcher;
import com.wally.wally.datacontroller.firebase.geofire.GeoUtils;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DebugUtils {
    public static final String TAG = DebugUtils.class.getSimpleName();
    public static final User[] USERS = new User[]{
            new User("bPwMCPf2MWbebkLQrUuXKw3kYjW2").withGgId(""),  // Io
            new User("8g7t26liJZgP6Z7jHgTkTdZLk632").withGgId("114669062093261610699"), // George
            new User("50tSKKashRRrbPP3fKtOWI9vpRg1").withGgId(""), // Tango
            new User("uSlLJUtZqbRDTMeLU4MdcToS8ZZ2").withGgId("112058086965911533829"), // Misha
    };
    public static final User DEBUG_USER = USERS[3];
    public static final LatLng OFFICE_LAT_LNG = new LatLng(41.8057582f, 44.7681694f);
    public static String[] ROOMS = new String[]{
            "a", "b", "c", "d", "e",
            "f", "g", "h", "i", "j",
            "k", "l", "m", "n", "o"
    };

    private static SecureRandom random = new SecureRandom();


    public static Content generateRandomContent() {
        return new Content()
                .withUuid(randomStr(1))
                .withNote(randomStr(15))
                .withTitle(randomStr(7))
                .withColor(random.nextInt())
                .withImageUri("http://" + randomStr(10))
                .withAuthorId(USERS[random.nextInt(4)].getId().getId())
                .withLocation(randomLatLngNearPoint(OFFICE_LAT_LNG))
                .withVisibility(
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
        int publicContentNumber = 0;
        for (int i = 0; i < n; i++) {
            Content content = generateRandomContent();
            if (content.isPublic()) {
                content.withTitle("" + publicContentNumber++);
            } else if (!content.isPrivate()) {
                List<Id> sharedWith = new ArrayList<>();
                sharedWith.add(USERS[1].getGgId());
                sharedWith.add(USERS[3].getGgId());
                content.getVisibility().getSocialVisibility().withSharedWith(sharedWith);
            }
            controller.save(content.withImageUri(null));
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
        return new Double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()};
    }

    public static LatLng randomLatLngNearPoint(LatLng point) {
        double randomLat = point.latitude + nextSign() * (random.nextDouble() % 100) / 500;
        double randomLng = point.longitude + nextSign() * (random.nextDouble() % 100) / 500;
        return new LatLng(randomLat, randomLng);
    }

    private static int nextSign() {
        return random.nextBoolean() ? -1 : 1;
    }

    // Warning: Does not work as u expect
    public static String randomStr(int length) {
        return new BigInteger(130, random).toString(32).substring(0, length);
    }

    public static boolean randomBool() {
        return random.nextBoolean();
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
//        Log.d(tag, c.getId());
//        Log.d(tag, c.getTitle());

        double diff = GeoUtils.distance(OFFICE_LAT_LNG, c.getLocation());
//        LatLng l = c.getLocation();
//        Log.d(tag, new GeoHash(l.latitude, l.longitude).getGeoHashString() + ": " + diff);

        Log.d(tag, c.getAuthorId() + " (" + c.getVisibility().getSocialVisibility().getMode() + ") " + diff);

    }

    public static FetchResultCallback debugCallback() {
        return debugCallback(DataController.TAG);
    }

    public static FetchResultCallback fetchNextDebugCallback(
            final int count, final ContentFetcher fetcher, final FetchResultCallback callback) {
        return new FetchResultCallback() {

            @Override
            public void onResult(Collection<Content> result) {
                DebugUtils.debugCallback().onResult(result);
                fetcher.fetchNext(count, callback);
            }

            @Override
            public void onError(Exception e) {
            }
        };
    }

    public static List<AdfMetaData> generateRandomAdfMetaData(int quantity) {
        List<AdfMetaData> list = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            list.add(new AdfMetaData(
                    randomStr(10),
                    randomStr(20),
                    randomLatLngNearPoint(OFFICE_LAT_LNG)));
        }
        return list;
    }

    public static void refreshContents(DatabaseReference contents, DataController datacontroller) {
        contents.removeValue();
        generateRandomContents(100, datacontroller);
    }

    public static void sanityCheck(DataController datacontroller) {
        ContentFetcher fetcher = datacontroller.createFetcherForVisibleContent(OFFICE_LAT_LNG, 10);
        fetcher.fetchNext(7,
                fetchNextDebugCallback(7, fetcher,
                        fetchNextDebugCallback(100, fetcher,
                                fetchNextDebugCallback(7, fetcher,
                                        debugCallback()))));
    }
}