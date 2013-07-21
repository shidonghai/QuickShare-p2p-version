package com.cloudsynch.quickshare.utils;

import android.content.Context;
import com.cloudsynch.quickshare.netresources.Networks;
import com.cloudsynch.quickshare.resource.module.ResourceCategory;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Xiaohu on 13-6-30.
 */
public class EventManager {

    private Context mContext;

    private static EventManager mInstance;

    private EventManager() {
    }

    public static EventManager getInstance() {
        if (null == mInstance) {
            mInstance = new EventManager();
        }

        return mInstance;
    }

    public void init(Context context) {
        mContext = context;

        EventConstant.TRANSPORT_TYPES.put(ResourceCategory.Category.AUDIO, EventConstant.TRANSPORT_AUDIO);
        EventConstant.TRANSPORT_TYPES.put(ResourceCategory.Category.VIDEO, EventConstant.TRANSPORT_VIEDO);
        EventConstant.TRANSPORT_TYPES.put(ResourceCategory.Category.APK, EventConstant.TRANSPORT_APP);
        EventConstant.TRANSPORT_TYPES.put(ResourceCategory.Category.DOC, EventConstant.TRANSPORT_DOC);
        EventConstant.TRANSPORT_TYPES.put(ResourceCategory.Category.PHOTO, EventConstant.TRANSPORT_PHOTO);
        EventConstant.TRANSPORT_TYPES.put(ResourceCategory.Category.ZIP, EventConstant.TRANSPORT_COMPRESS);

        EventConstant.NET_SOURCE_TYPES.put(Networks.Type.NEW, EventConstant.NET_SOURCE_NEW);
        EventConstant.NET_SOURCE_TYPES.put(Networks.Type.CARTOON, EventConstant.NET_SOURCE_CARTOON);
        EventConstant.NET_SOURCE_TYPES.put(Networks.Type.TVPLAY, EventConstant.NET_SOURCE_TVPLAY);
        EventConstant.NET_SOURCE_TYPES.put(Networks.Type.MOVIE, EventConstant.NET_SOURCE_MOVIE);
    }

    public void onEvent(String eventId) {
        if (null != mContext) {
            MobclickAgent.onEvent(mContext, eventId);
        }
    }


}
