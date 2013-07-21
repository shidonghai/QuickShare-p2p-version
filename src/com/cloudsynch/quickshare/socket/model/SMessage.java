package com.cloudsynch.quickshare.socket.model;

import java.io.Serializable;

import android.util.Log;

import com.google.gson.Gson;

public abstract class SMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int MSG_DISCOVER = 1;
	public static final int MSG_FILE_TRANSFER = 2;
	public static final int MSG_HEART_BEAT = 3;
	public static final int MSG_STATUS_UPDATE = 4;

	public static final int REQ = 0;
	public static final int ACK = 1;

	protected int mMsgType;
	protected int mMsgDirection = REQ;
	protected int mMsgId = 0;

	// data stored in JSON string
	protected String mData = null;

	// address and port of remote machine
	protected String mRemoteAddress = null;
	protected int mRemotePort = 0;

	public void setMessageID(int id) {
		mMsgId = id;
	}

	public int getMessageID() {
		return mMsgId;
	}

	public void setRemoteAdress(String IP) {
		mRemoteAddress = IP;
	}

	public void setRemotePort(int port) {
		mRemotePort = port;
	}

	public String getRemoteAddress() {
		return mRemoteAddress;
	}

	public int getRemotePort() {
		return mRemotePort;
	}

	public int getMsgDirection() {
		return mMsgDirection;
	}

	public void setMsgDirection(int direction) {
		mMsgDirection = direction;
	}

	public int getMessageType() {
		return mMsgType;
	}

	public String getData() {
		return mData;
	}

	public void setData(String jsonData) {
		mData = jsonData;
	}

	public void dump() {
		Log.w("ShareApp_dump", toJsonString());
	}

	public String toJsonString() {
		Gson gson = new Gson();
		String str = gson.toJson(this);
		return str;
	}
}
