package com.cloudsynch.quickshare.resource.module;

import com.cloudsynch.quickshare.resource.ui.ResourceDetailBaseFragment;
import com.cloudsynch.quickshare.resource.ui.ResourceDetailCursorFragment;
import com.cloudsynch.quickshare.resource.ui.ResourceDetailFileFragment;

/**
 * Created by Xiaohu on 13-6-5.
 */
public class ResourceDetailFactory {

    public static ResourceDetailBaseFragment getDetailFragment(ResourceCategory.Category category) {
        ResourceDetailBaseFragment fragment;
        switch (category) {
            case QUICK_SHARE:
            case BLUETOOTH:
                fragment = new ResourceDetailFileFragment();
                break;
            default:
                fragment = new ResourceDetailCursorFragment();
                break;
        }

        return fragment;
    }
}
