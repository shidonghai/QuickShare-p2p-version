package com.cloudsynch.quickshare.socket.transfer;

import java.io.Serializable;

public class FileInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4859101469056142283L;
	public String name;
	public String path;
	public int type;
	public long length;
}
