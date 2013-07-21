package com.cloudsynch.quickshare.netresources;

import com.cloudsynch.quickshare.http.AsyncHttpClient;
import com.cloudsynch.quickshare.http.AsyncHttpResponseHandler;

public class NetworkTask<T> {
	private AsyncHttpClient mClient;
	private ResultParser<T> mParser;
	private Callback<T> mCallback;

	public NetworkTask() {
		mClient = new AsyncHttpClient();
	}

	public void request(String url) {
		mClient.get(url, new AsyncHttpResponseHandler<T>(mParser) {
			public void onSuccess(Result<T> content) {
				if (mCallback != null) {
					mCallback.onSuccess(content);
				}
			}

			public void onFailure(Throwable error, String content) {
				if (mCallback != null) {
					mCallback.onFail(error, content);
				}
			}
		});
	}

	public NetworkTask<T> setParser(ResultParser<T> parser) {
		mParser = parser;
		return this;
	}

	public NetworkTask<T> setCallback(Callback<T> callback) {
		mCallback = callback;
		return this;
	}

	public static interface Callback<T> {
		public void onSuccess(Result<T> result);

		public void onFail(Throwable error, String content);
	}
}
