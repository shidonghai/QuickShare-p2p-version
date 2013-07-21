package com.cloudsynch.quickshare.socket.model;

public class FileTransferMessage extends SMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * transfer Message format, example
	 * 
	 * {"message" : 2, "data" : { "type" : 0; //0 for REQ, 1 for ACK
	 * "messageId": "001" //identify the request "name" : "user name" //nick
	 * name "macAddr" : "local MAC address" //user identifier "MimeType":
	 * "text/plain" //mime type of the file "FileLength": 1024 //file length of
	 * the file "FilePath": "/user/beijing.txt" //local file path "targetIP":
	 * "19.168.1.1" //target IP address "targetPort": 808 //target port }, "md5"
	 * : " " //md5 digest, optional, for future security requirement }
	 */
	public FileTransferMessage() {
		mMsgType = MSG_FILE_TRANSFER;
	}

	private String mName = null;
	private String mMIMEType = null;
	private long mFileLen = 0;
	private String mFilePath = null;
	private int mTargetPort;
	private String mTargetIp = null;
	private String mTaskInfo = null;

	public long getFileLength() {
		return mFileLen;
	}

	public void setFileLength(long length) {
		mFileLen = length;
	}

	public String getMimeType() {
		return mMIMEType;
	}

	public void setMimeType(String type) {
		mMIMEType = type;
	}

	public String getTargetIp() {
		return mTargetIp;
	}

	public void setTargetIp(String targetIp) {
		mTargetIp = targetIp;
	}

	public void setTargetPort(int port) {
		mTargetPort = port;
	}

	public int getTargetPort() {
		return mTargetPort;
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setFilePath(String filePath) {
		mFilePath = filePath;
	}

	public String toString() {
		return "filepath:" + mFilePath;
	}

	public void setTaskInfo(String string) {
		mTaskInfo = string;
	}

	public String getTaskInfo() {
		return mTaskInfo;
	}

}
