package com.cloudsynch.quickshare.utils;

import com.cloudsynch.quickshare.resource.module.ResourceCategory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xiaohu on 13-6-30.
 */
public class EventConstant {

    public static final String MENU_SETTING = "MENU_SETTING";

    public static final String MENU_RESOURCE_MANAGER = "MENU_RESOURCE_MANAGER";

    public static final String MENU_NET_RESOURCE = "MENU_NET_RESOURCE";

    public static final String MENU_USER_INFO = "MENU_USER_INFO";

    public static final String USER_EDIT = "USER_EDIT";

    public static final String USER_JOIN = "USER_JOIN";

    public static final String USER_SHARE = "USER_SHARE";

    public static final String USER_HISTORY = "USER_HISTORY";

    public static final String INVITE_SMS = "INVITE_SMS";

    public static final String INVITE_BLUETOOTH = "INVITE_BLUETOOTH";

    public static final String INVITE_ZERO_FLOW = "INVITE_ZERO_FLOW";

    public static final String INVITE_SINA = "INVITE_SINA";

    public static final String INVITE_TENCENT = "INVITE_TENCENT";

    public static final String INVITE_RENREN = "INVITE_RENREN";

    public static final String RESOURCE_MANAGER_COMPRESS = "RESOURCE_MANAGER_COMPRESS";

    public static final String RESOURCE_MANAGER_BLUETOOTH = "RESOURCE_MANAGER_BLUETOOTH";

    public static final String RESOURCE_MANAGER_QUICKSHARE = "RESOURCE_MANAGER_QUICKSHARE";

    public static final String RESOURCE_MANAGER_DOC = "RESOURCE_MANAGER_DOC";

    public static final String RESOURCE_MANAGER_APP = "RESOURCE_MANAGER_APP";

    public static final String RESOURCE_MANAGER_PHOTO = "RESOURCE_MANAGER_PHOTO";

    public static final String RESOURCE_MANAGER_AUDIO = "RESOURCE_MANAGER_AUDIO";

    public static final String RESOURCE_MANAGER_VIDEO = "RESOURCE_MANAGER_VIDEO";

    public static final String[] RESOURCE_MANAGER_IDS = new String[]{RESOURCE_MANAGER_VIDEO, RESOURCE_MANAGER_AUDIO, RESOURCE_MANAGER_PHOTO
            , RESOURCE_MANAGER_APP, RESOURCE_MANAGER_DOC, RESOURCE_MANAGER_QUICKSHARE, RESOURCE_MANAGER_BLUETOOTH, RESOURCE_MANAGER_COMPRESS};

    public static final String TRANSPORT_COMPRESS = "TRANSPORT_COMPRESS";

    public static final String TRANSPORT_DOC = "TRANSPORT_DOC";

    public static final String TRANSPORT_APP = "TRANSPORT_APP";

    public static final String TRANSPORT_PHOTO = "TRANSPORT_PHOTO";

    public static final String TRANSPORT_AUDIO = "TRANSPORT_AUDIO";

    public static final String TRANSPORT_VIEDO = "TRANSPORT_VIEDO";

    public static final Map<ResourceCategory.Category, String> TRANSPORT_TYPES = new HashMap<ResourceCategory.Category, String>();

    public static final String NET_SOURCE_CARTOON = "NET_SOURCE_CARTOON";

    public static final String NET_SOURCE_TVPLAY = "NET_SOURCE_TVPLAY";

    public static final String NET_SOURCE_MOVIE = "NET_SOURCE_MOVIE";

    public static final String NET_SOURCE_NEW = "NET_SOURCE_NEW";

    public static final Map<String, String> NET_SOURCE_TYPES = new HashMap<String, String>();

    public static final String NET_SOURCE_DOWNLOAD = "NET_SOURCE_DOWNLOAD";

}
