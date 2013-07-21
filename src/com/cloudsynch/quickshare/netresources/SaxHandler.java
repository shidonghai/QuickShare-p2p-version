package com.cloudsynch.quickshare.netresources;

import org.xml.sax.helpers.DefaultHandler;

public abstract class SaxHandler<T> extends DefaultHandler {
	protected Result<T> mResult;

	public SaxHandler() {
		mResult = new Result<T>();
	}

	public Result<T> getResult() {
		return mResult;
	}

	public void addItem(T item) {
		mResult.addItem(item);
	}

}
