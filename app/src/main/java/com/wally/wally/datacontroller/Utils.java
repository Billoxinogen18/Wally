package com.wally.wally.datacontroller;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.callbacks.FetchResultCallback;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.user.User;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UnknownFormatConversionException;

public class Utils {
    private static SecureRandom random = new SecureRandom();

    // This method is for debugging purposes
    @SuppressWarnings("unused")
    public static Content  generateRandomContent() {
        return new Content()
                .withUuid(randomStr(10))
                .withNote(randomStr(20))
                .withTitle(randomStr(20))
                .withImageUri("http://" + randomStr(20))
                .withAuthor(
                        new User("uSlLJUtZqbRDTMeLU4MdcToS8ZZ2")
                                .withGgId("112058086965911533829")
                ).withLocation(
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
                                .withScale(random.nextInt())
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

    private static double[] randomDoubleArray() {
        return new double[] {
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble()
        };
    }

    // Warning: Does not work as u expect
    public static String randomStr(int length) {
        return new BigInteger(130, random).toString(32).substring(0, length);
    }

    public static List<Double> arrayToList(double[] array) {
        List<Double> list = new ArrayList<>();
        for (double d : array) list.add(d);
        return list;
    }

    public static double[] listToArray(List<Double> list) {
        double[] array = new double[list.size()];
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
                    String publicity = c.getVisibility().getSocialVisibility().toString().substring(22);
                    Log.d(tag, "{" + c.getTitle().substring(0, 2) + " - " + publicity);
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
