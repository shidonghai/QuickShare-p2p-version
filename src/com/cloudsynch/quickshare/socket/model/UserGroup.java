package com.cloudsynch.quickshare.socket.model;

import java.io.Serializable;

public class UserGroup implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public User host;

	@Override
	public boolean equals(Object other) {
		boolean flag = other instanceof UserGroup;
		if (flag) {
			UserGroup user = (UserGroup) other;
			return host.equals(user.host);
		} else {
			return flag;
		}
	}
}
