package com.wally.wally.datacontroller;

import com.google.android.gms.maps.model.LatLng;
import com.wally.wally.datacontroller.content.Content;
import com.wally.wally.datacontroller.content.TangoData;
import com.wally.wally.datacontroller.content.Visibility;
import com.wally.wally.datacontroller.user.User;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static SecureRandom random = new SecureRandom();

    // This method is for debugging purposes
    @SuppressWarnings("unused")
    public static Content  generateRandomContent() {
        return new Content()
                .withTitle(randomStr(20))
                .withNote(randomStr(20))
                .withImageUri("http://" + randomStr(20))
                .withUuid("uuid: "+ randomStr(10))
                .withAuthor(new User("t3YIz86JPzb6KRvnLJnmuGpxGSr1")
                        .withGgId("t3YIz86JPzb6KRvnLJnmuGpxGSr1"))
                .withLocation(new LatLng(random.nextInt(), random.nextInt()))
                .withVisibility(new Visibility()
                        .withVisiblePreview(random.nextBoolean())
                        .withTimeVisibility(null)
                        .withRangeVisibility(new Visibility.RangeVisibility(Visibility.RangeVisibility.FAR))
                        .withSocialVisibility(new Visibility.SocialVisibility(
                                Visibility.SocialVisibility.FRIENDS))
                ).withTangoData(new TangoData()
                        .withScale(random.nextInt())
                        .withRotation(new double[] {
                                random.nextDouble(),
                                random.nextDouble(),
                                random.nextDouble()})
                        .withTranslation(new double[] {
                                random.nextDouble(),
                                random.nextDouble(),
                                random.nextDouble()})
                );
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

}
