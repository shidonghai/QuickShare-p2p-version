package com.cloudsynch.quickshare.netresources;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.text.TextUtils;

import com.cloudsynch.quickshare.utils.LogUtil;

public class AlbumParser extends ResultParser<Album> {
	private String TAG = AlbumParser.class.getName();
	private SAXParser mParser;
	private AlbumParserHandler mHandler = new AlbumParserHandler();

	public AlbumParser() {
		try {
			mParser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void parse(String content) {
		StringReader reader = new StringReader(content);
		if (mParser != null)
			try {
				mParser.parse(new InputSource(reader), mHandler);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private String removeBadContent(String content) {
		String startTag = "<type>";
		String endTag = "</count>";

		String download = "<download>";

		int start = content.indexOf(startTag);
		int end = content.indexOf(endTag) + endTag.length();

		int index = content.lastIndexOf(download);

		StringBuilder builder = new StringBuilder(content);

		String result = builder.delete(index, index + download.length())
				.delete(start, end).toString();
		LogUtil.e(TAG, result);
		return result;
	}

	@Override
	public Result<Album> getResult() {
		return mHandler.getResult();
	}

	class AlbumParserHandler extends SaxHandler<Album> {
		private Album album;
		private Download download;

		private String thisTag;

		private boolean albumFlag;
		private boolean downloadFlag;

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			LogUtil.e(TAG, "start element:" + qName);
			thisTag = qName;
			if ("items".equals(qName)) {
				albumFlag = true;
			} else if ("download".equals(qName)) {
				downloadFlag = true;
			} else if ("item".equals(qName)) {
				if (downloadFlag) {
					download = new Download();
				} else if (albumFlag) {
					album = new Album();
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			LogUtil.e(TAG, "end element:" + qName);
			if ("item".equals(qName)) {
				if (downloadFlag && download != null) {
					if (download.download_url.contains("hd.data"))
						album.downloadHd.add(download);
					else
						album.download.add(download);
					LogUtil.e(TAG, "add download info==========");
					download = null;
				} else if (albumFlag && album != null) {
					addItem(album);
					LogUtil.e(TAG, "add album info==========");
				}
			}
			if ("download".equals(qName)) {
				downloadFlag = false;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String content = new String(ch, start, length);

			if (TextUtils.isEmpty(content.trim())) {
				LogUtil.e(TAG, "content is null");
				return;
			}
			if (downloadFlag && download != null) {
				try {
					Field field = download.getClass().getField(thisTag);
					field.set(download, content);
					LogUtil.e(TAG, "download-----" + thisTag + ":" + content);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (albumFlag && album != null) {
				try {
					Field field = album.getClass().getField(thisTag);
					field.set(album, content);
					LogUtil.e(TAG, "album-----" + thisTag + ":" + content);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
