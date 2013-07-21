package com.cloudsynch.quickshare.socket.model;

import java.io.Serializable;

import com.cloudsynch.quickshare.utils.LogUtil;
import com.google.gson.Gson;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	public interface Gender {
		public static final int BOY = 1;
		public static final int GIRL = 0;
		public static final int OTHER = 10;
	}

	public interface Status {
		public static final int ONLINE = 1;
		public static final int OFFLINE = 0;
	}

	// user status
	public int status = Status.OFFLINE;

	// has new message come from this user
	public boolean hasNewMessage = false;

	// nicky name of user
	public String name = "unkown";

	// gender
	public int gender = Gender.OTHER;

	public String signature = "nothing to say";

	// unique identifier of a user, like MAC address
	public String identifier = null;

	// ip
	public String ip = "0.0.0.0";

	// user photo data
	public String photo = null;

	@Override
	public String toString() {
		Gson gson = new Gson();
		String jsonStr = gson.toJson(this);
		LogUtil.e("json", jsonStr);
		return jsonStr;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof User) {
			User user = (User) other;
			if (identifier == null || user.identifier == null) {
				if (ip == null || user.ip == null) {
					return false;
				} else {
					return ip.equals(user.ip);
				}
			}
			return identifier.equals(user.identifier);
		}
		return false;
	}

	public void update(User user) {
		this.gender = user.gender;
		this.hasNewMessage = user.hasNewMessage;
		this.identifier = user.identifier;
		this.name = user.name;
		this.photo = user.photo;
		this.signature = user.signature;
		this.status = user.status;
		this.ip = user.ip;
	}
}
