package com.cloudsynch.quickshare.socket.model;

public class DiscoveryMessage extends SMessage {
	/*
	 * discovery Message format, example
	 * 
	 * {"message" : 1, "data" : { "type" : 0; //0 for REQ, 1 for ACK "name" :
	 * "user name" //nick name "photo" : "base64 data" //photo thumb nail
	 * "macAddr" : "local MAC address" //user identifier }, "md5" : " " //md5
	 * digest, optional, for future security requirement }
	 */

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DiscoveryMessage() {
		mMsgType = MSG_DISCOVER;
	}

}
