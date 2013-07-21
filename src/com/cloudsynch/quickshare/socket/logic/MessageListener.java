package com.cloudsynch.quickshare.socket.logic;

import com.cloudsynch.quickshare.socket.model.SMessage;

public abstract class MessageListener {
	public static final int MSG_USER_DISCOVER_REQ = 0x0001;
	public static final int MSG_USER_DISCOVER_ACK = 0x0002;
	public static final int MSG_TRANSFER_REQUEST = 0x0004;
	public static final int MSG_TRANSFER_CONFIRM = 0x0008;
	public static final int MSG_USER_STATUS_UPDATE = 0x0010;

	protected int mEventFilter = 0;

	public void addFilterType(int type) {
		mEventFilter = mEventFilter | type;
	}

	public abstract void onEvent(int type, SMessage msg);
}
