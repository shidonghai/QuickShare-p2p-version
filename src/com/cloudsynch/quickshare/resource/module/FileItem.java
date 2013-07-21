package com.cloudsynch.quickshare.resource.module;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Xiaohu on 13-6-6.
 */
public class FileItem implements Serializable {

    public FileItem(long aId, boolean aIsSeletced, File aFile) {
        id = aId;
        isSeletced = aIsSeletced;
        file = aFile;
    }

    public long id;

    public boolean isSeletced;

    public File file;
}
