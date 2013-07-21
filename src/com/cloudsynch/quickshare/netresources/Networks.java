package com.cloudsynch.quickshare.netresources;

public class Networks {
	private class Url {
		public static final String BASE_URL = "http://api.yuntongbu.com/a67/get_data?type=%s&offset=%d&count=%d";
	}

	public class Type {
		public static final String NEW = "newsdata";
		public static final String MOVIE = "mov";
		public static final String CARTOON = "dm";
		public static final String TVPLAY = "tv";
	}

	public static class UrlBuilder {

		public static String build(String type, int offset, int count) {
			return String.format(Url.BASE_URL, type, offset, count);
		}
	}
}
