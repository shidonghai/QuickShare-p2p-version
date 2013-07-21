package com.cloudsynch.quickshare.resource.logic;

import java.io.File;
import java.util.List;

/**
 * Created by Xiaohu on 13-6-6.
 */
public interface IMultipleSelection {
	void startMultipleSelect();

	void cancelMultipleSelect();

	List<File> getSelectItems();

	boolean isMultipleSelectMode();

	void setMultipleSelectMode(boolean isSelectMode);

	void selectAll(boolean isAll);

	void onChange();

	int getAllCount();

}
