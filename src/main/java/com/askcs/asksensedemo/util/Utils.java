package com.askcs.asksensedemo.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

/**
 * Some static utility methods.
 */
public final class Utils {

    // The log tag.
    private static final String TAG = Utils.class.getName();

    // Private constructor: no need to instantiate this class.
    private Utils() {
    }

    /**
     * Creates an MD5 hash from a given string.
     *
     * @param input the string to generate the MD% hash from.
     * @return an MD5 hash from a given string.
     * @throws RuntimeException if the MD5 hashing algorithm is not
     *                          available (very unlikely though).
     */
    public static String md5(String input) {

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);

            String hex = number.toString(16);
            StringBuilder md5Builder = new StringBuilder(hex);

            // Prepend zeros as long as the builder contains less than 32 chars.
            while (md5Builder.length() < 32) {
                md5Builder.insert(0, '0');
            }

            return md5Builder.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }
}
