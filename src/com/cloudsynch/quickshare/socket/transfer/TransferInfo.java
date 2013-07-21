package com.cloudsynch.quickshare.socket.transfer;

import java.io.Serializable;

import com.cloudsynch.quickshare.socket.logic.TransferController.TransferTask;

public class TransferInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2955846054439121489L;

	public TransferTask task;
	public int percent;
	public int direction;
}
