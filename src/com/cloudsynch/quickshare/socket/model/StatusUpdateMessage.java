package com.cloudsynch.quickshare.socket.model;

public class StatusUpdateMessage extends SMessage {
	/*
	 * user status update Message format, example
	 * 
	 * {"message" : 4, "data" : { "type" : 0, for REQ(0)/ACK(1) "name" :
	 * "user name" //nick name "macAddr" : "local MAC address" //user identifier
	 * "userStatus": 0 //user status like offline/online/hide }, "md5" : " "
	 * //md5 digest, optional, for future security requirement }
	 */

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int mStatus = 0;

	public void setStatus(int status) {
		mStatus = status;
	}

	public int getStatus() {
		return mStatus;
	}

}
