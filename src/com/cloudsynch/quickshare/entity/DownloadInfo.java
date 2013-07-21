
package com.cloudsynch.quickshare.entity;

public class DownloadInfo {
    public int id;
    public String url;
    public String fileName;
    public String savePath;
    public int date;
    public int receSize;
    public int totalSize;
    public int status;

    public static class Status {
        public static final int STOP = 0;
        public static final int START = 1;
        public static final int FINISH = 2;
        public static final int ERROT = 3;

    }
}
