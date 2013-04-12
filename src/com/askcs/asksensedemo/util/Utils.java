package com.askcs.asksensedemo.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

public final class Utils {
	
	private static final String TAG = Utils.class.getName();
	
	private Utils() {
		// No need to instantiate this class.
	}
	
	public static String md5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);
		 
			while (md5.length() < 32) {
				md5 = "0" + md5;
			}
		 
			return md5;
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getLocalizedMessage());
			return null;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(Utils.md5("!Amk220Bk284"));
	}
}
