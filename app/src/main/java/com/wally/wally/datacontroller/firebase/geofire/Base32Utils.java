package com.wally.wally.datacontroller.firebase.geofire;

/**
 * Original source of the class is part of GeoFire library
 * code was copied and modified to meet our needs
 * To see original source files follow the link:
 * https://github.com/firebase/geofire-java
 */
public class Base32Utils {

    /* number of bits per base 32 character */
    public static final int BITS_PER_BASE32_CHAR = 5;

    private static final String BASE32_CHARS = "0123456789bcdefghjkmnpqrstuvwxyz";

    private Base32Utils() {}

    public static char valueToBase32Char(int value) {
        if (value < 0 || value >= BASE32_CHARS.length()) {
            throw new IllegalArgumentException("Not a valid base32 value: " + value);
        }
        return BASE32_CHARS.charAt(value);
    }

    public static int base32CharToValue(char base32Char) {
        int value = BASE32_CHARS.indexOf(base32Char);
        if (value == -1) {
            throw new IllegalArgumentException("Not a valid base32 char: " + base32Char);
        } else {
            return value;
        }
    }

}
