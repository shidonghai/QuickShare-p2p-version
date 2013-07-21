package com.cloudsynch.quickshare.socket.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import com.cloudsynch.quickshare.socket.model.User;
import com.cloudsynch.quickshare.socket.transfer.FileInfo;
import com.cloudsynch.quickshare.utils.LogUtil;
import com.google.gson.Gson;

public class TransferController {
	private HashMap<User, Queue<FileInfo>> mTaskMap = new HashMap<User, Queue<FileInfo>>();

	public TransferController() {

	}

	public void offer(ArrayList<FileInfo> files, ArrayList<User> users) {
		for (User user : users) {
			LinkedBlockingQueue<FileInfo> q = new LinkedBlockingQueue<FileInfo>();
			for (FileInfo info : files) {
				LogUtil.e("offer task", "add file " + info.name + " for user "
						+ user.identifier);
				q.offer(info);
			}
			mTaskMap.put(user, q);
		}
	}

	public TransferTask pollTask(User user) {

		User targetUser = null;
		Set<User> list = getUserList();
		for (User key : list) {
			if (key.equals(user)) {
				targetUser = key;
			}
		}
		if (targetUser == null) {
			LogUtil.e("taskMap", "found no key");
			return null;
		}

		Queue<FileInfo> q = mTaskMap.get(targetUser);
		if (q == null) {
			LogUtil.e("taskMap", "queue is empty");
			return null;
		}
		FileInfo info = q.poll();
		if (info == null) {
			LogUtil.e("taskMap", "no more task");
			return null;
		}
		return new TransferTask(info, targetUser);
	}

	public static class TransferTask implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2221394546895902520L;
		public User from;
		public User to;
		public FileInfo info;

		public TransferTask(FileInfo info, User user) {
			this.to = user;
			this.info = info;
		}

		@Override
		public String toString() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}

	public Set<User> getUserList() {
		return mTaskMap.keySet();
	}

}
