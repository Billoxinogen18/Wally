package com.wally.wally.datacontroller;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.datacontroller.content.Visibility;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UnknownFormatConversionException;

public class Utils {
    public static final String DEBUG_USER_ID = "uSlLJUtZqbRDTMeLU4MdcToS8ZZ2";
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
                .withAuthorId(DEBUG_USER_ID)
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

    public static List<Double> arrayToList(Double[] array) {
        List<Double> list = new ArrayList<>();
        Collections.addAll(list, array);
        return list;
    }

    public static Double[] listToArray(List<Double> list) {
        Double[] array = new Double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static FetchResultCallback debugCallback(final String tag) {
        return new FetchResultCallback() {
            @Override
            public void onResult(Collection<Content> result) {
                Log.d(tag, "" + result.size());
                for (Content c : result) {
//                    String publicity = c.getVisibility().getSocialVisibility().toString().substring(22);
//                    Log.d(tag, "{" + c.getTitle().substring(0, 2) + " - " + publicity);
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

    public static double toDouble(Object obj) {

        if (obj instanceof Double)
            return (double) obj;

        if (obj instanceof Long)
            return (double) (long) obj;

        if (obj instanceof Integer)
            return (double) (int) obj;

        throw new UnknownFormatConversionException("Cannot convert to double");
    }

}
