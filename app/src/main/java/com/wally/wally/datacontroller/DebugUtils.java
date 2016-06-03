package com.wally.wally.datacontroller;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.user.Id;
import com.wally.wally.datacontroller.user.User;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DebugUtils {
    public static final Id DEBUG_USER_ID =
            new Id(Id.PROVIDER_FIREBASE, "uSlLJUtZqbRDTMeLU4MdcToS8ZZ2");
    public static final User DEBUG_USER = new User(DEBUG_USER_ID.getId()).withGgId("");
    private static SecureRandom random = new SecureRandom();

    // This method is for debugging purposes
    @SuppressWarnings("unused")
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
                                random.nextInt(),
                                random.nextInt()
                        )
                ).withVisibility(
                        new Visibility()
                                .withTimeVisibility(null)
                                .withRangeVisibility(randomRange())
                                .withSocialVisibility(randomPublicity())
                                .withVisiblePreview(random.nextBoolean())
                ).withTangoData(
                        new TangoData()
                                .withScale((double) random.nextInt())
                                .withRotation(randomDoubleArray())
                                .withTranslation(randomDoubleArray()));
    }

    private static Visibility.RangeVisibility randomRange() {
        //noinspection WrongConstant
        return new Visibility.RangeVisibility(
                Math.abs(random.nextInt()) % 5);
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
                    Log.d(tag, c.toString());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(tag, "Shit gone wrong!");
            }
        };
    }

    public static FetchResultCallback debugCallback() {
        return debugCallback(DataController.TAG);
    }

//    private void testFetchAccesibleContent() {
//
//        Content my = DebugUtils.generateRandomContent()
//                .withAuthorId(DebugUtils.DEBUG_USER_ID.getId());
//        save(my);
//
//        Content friends = DebugUtils.generateRandomContent().withAuthorId("friends author id");
//        List<Id> sharedWithMe = new ArrayList<>();
//        sharedWithMe.add(DebugUtils.DEBUG_USER_ID);
//        friends.getVisibility().getSocialVisibility().withSharedWith(sharedWithMe);
//        save(friends);
//
//        Content publc = DebugUtils.generateRandomContent().withAuthorId("Somone else");
//        publc.getVisibility().getSocialVisibility().setMode(Visibility.SocialVisibility.PUBLIC);
//        save(publc);
//
//        fetchAccessibleContent(DebugUtils.DEBUG_USER, DebugUtils.debugCallback());
//    }

}