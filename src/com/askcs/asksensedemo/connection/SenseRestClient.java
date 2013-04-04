package com.askcs.asksensedemo.connection;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public final class SenseRestClient {
	
	private static final String TAG = SenseRestClient.class.getName();
	
	private static final String BASE_URL = "http://api.sense-os.nl/";

	private static AsyncHttpClient client = new AsyncHttpClient();
	
	private SenseRestClient() {
		// No need to instantiate this class.
	}
	
	public static void login(String username, String md5Hash, AsyncHttpResponseHandler responseHandler) {

		RequestParams params = new RequestParams();
		params.put("username", username);
		params.put("password", md5Hash);
		
		String url = BASE_URL + "login/";
		
		Log.i(TAG, "making a call to: `" + url + "` with params: " + params);
		
		client.post(url, params, responseHandler);
	}
}
