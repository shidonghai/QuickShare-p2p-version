package com.cloudsynch.quickshare.netresources;

import java.util.ArrayList;

public class Result<T> {
	public String type;
	public ArrayList<T> album;

	public Result() {
		album = new ArrayList<T>();
	}

	public int getCount() {
		return album.size();
	}

	public void addItem(T item) {
		if (!album.contains(item)) {
			album.add(item);
		}
	}

	public void addAll(Result<T> result) {
		ArrayList<T> list = result.album;
		for (T a : list) {
			addItem(a);
		}
	}
}
