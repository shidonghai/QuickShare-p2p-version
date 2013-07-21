package com.cloudsynch.quickshare.resource.module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.cloudsynch.quickshare.R;

/**
 * Created by Xiaohu on 13-5-30.
 */
public class ResourceCategory implements Serializable {
    public enum Category {
        VIDEO, AUDIO, PHOTO, APK, DOC, QUICK_SHARE, BLUETOOTH, ZIP, OTHER;

        public int getStringId() {
            switch (this) {
                case VIDEO:
                    return R.string.resource_manager_video;
                case AUDIO:
                    return R.string.resource_manager_audio;
                case PHOTO:
                    return R.string.resource_manager_photo;
                case APK:
                    return R.string.resource_manager_apk;
                case DOC:
                    return R.string.resource_manager_doc;
                case QUICK_SHARE:
                    return R.string.resource_manager_a;
                case BLUETOOTH:
                    return R.string.resource_manager_bluetooth;
                case ZIP:
                    return R.string.resource_manager_zip;
                case OTHER:
                    return R.string.resource_manager_other;
            }
            return 0;
        }

        public int getImage() {
            switch (this) {
                case VIDEO:
                    return R.drawable.video_color;
                case AUDIO:
                    return R.drawable.music_color;
                case PHOTO:
                    return R.drawable.photo_color;
                case APK:
                    return R.drawable.installation_color;
                case DOC:
                    return R.drawable.document_color;
                case QUICK_SHARE:
                    return R.drawable.kuaichuan_color;
                case BLUETOOTH:
                    return R.drawable.bluetooth_color;
                case ZIP:
                    return R.drawable.compression_color;
                case OTHER:
                    return R.drawable.other_color;
            }
            return 0;
        }

        public int getColor() {
            switch (this) {
                case VIDEO:
                    return R.color.resource_video;
                case AUDIO:
                    return R.color.resource_audio;
                case PHOTO:
                    return R.color.resource_photo;
                case APK:
                    return R.color.resource_apk;
                case DOC:
                    return R.color.resource_doc;
                case QUICK_SHARE:
                    return R.color.resource_a;
                case BLUETOOTH:
                    return R.color.resource_bluetooth;
                case ZIP:
                    return R.color.resource_zip;
                case OTHER:
                    return R.color.resource_other;
            }
            return 0;
        }

        public int getIcon() {
            switch (this) {
                case VIDEO:
                    return R.drawable.resource_video_icon;
                case AUDIO:
                    return R.drawable.resource_music_icon;
                case PHOTO:
                    return R.drawable.resource_photo_icon;
                case APK:
                    return R.drawable.resource_installation_icon;
                case DOC:
                    return R.drawable.resource_document_icon;
                case QUICK_SHARE:
                    return R.drawable.resource_quickshare_icon;
                case BLUETOOTH:
                    return R.drawable.resource_bluetooth_icon;
                case ZIP:
                    return R.drawable.resource_compression_icon;
            }
            return 0;
        }

    }

    public ResourceCategory(Category aCategory) {
        category = aCategory;
    }

    public Category category;

    public long size;

    public int count;

    public List<FileItem> files = new ArrayList<FileItem>();

}
