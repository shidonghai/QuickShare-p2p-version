package com.cloudsynch.quickshare.netresources;

import java.io.Serializable;
import java.util.ArrayList;

public class Album implements Serializable {
	/**
	 * svUID
	 */
	private static final long serialVersionUID = 8925000314395286685L;
	public String album_type;
	public String uid = "";
	public String name;
	public String logo;
	public String year;
	public String area;
	public String type;
	public String score;
	public String director;
	public String actor;
	public String language;
	public String tips;
	public String play_time;
	public String intro;
	public String update_time;
	public String format;
	public String resolution;

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof Album) {
			Album other = (Album) o;
			return other.uid.equals(this.uid);
		}
		return false;
	}

	public ArrayList<Download> download = new ArrayList<Download>();
	public ArrayList<Download> downloadHd = new ArrayList<Download>();
}