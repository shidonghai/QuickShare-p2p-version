package com.cloudsynch.quickshare;

import java.util.ArrayList;

import android.content.Context;

import com.cloudsynch.quickshare.netresources.NetworkResourcesFragment;
import com.cloudsynch.quickshare.resource.ui.ResourceManagerFragment;
import com.cloudsynch.quickshare.settings.SettingsFragment;
import com.cloudsynch.quickshare.ui.HomeFragment;

/**
 * The factory class to generate the main menu.
 * 
 * @author KingBright
 */
public class MenuFactory {

	private static ArrayList<MenuItem> createMenu(Context ctx) {
		ArrayList<MenuItem> menu = new ArrayList<MenuItem>();
		menu.add(new MenuItem(R.drawable.portrait, R.string.default_username,
				0, null));
		menu.add(new MenuItem(R.drawable.home_icon, R.string.quickshare,
				R.string.quickshare_description, HomeFragment.class.getName()));
		menu.add(new MenuItem(R.drawable.network_icon,
				R.string.network_resources, R.string.network_description,
				NetworkResourcesFragment.class.getName()));
		menu.add(new MenuItem(R.drawable.resource_icon,
				R.string.resources_management, R.string.resources_description,
				ResourceManagerFragment.class.getName()));
		menu.add(new MenuItem(R.drawable.set_icon, R.string.settings,
				R.string.settings_description, SettingsFragment.class.getName()));
		return menu;
	}

	public static ArrayList<MenuItem> getMenu(Context ctx) {
		return createMenu(ctx);
	}

	static class MenuItem {
		public int drawable;
		public int title;
		public int description;
		public String fragmentName;

		public MenuItem(int drawable, int title, int description,
				String fragmentName) {
			this.drawable = drawable;
			this.title = title;
			this.description = description;
			this.fragmentName = fragmentName;
		}
	}
}
