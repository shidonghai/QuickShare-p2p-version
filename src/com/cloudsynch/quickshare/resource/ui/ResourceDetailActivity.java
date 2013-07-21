package com.cloudsynch.quickshare.resource.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.resource.ResourceManager;
import com.cloudsynch.quickshare.resource.module.ResourceCategory;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Xiaohu on 13-6-6.
 */
public class ResourceDetailActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource_detail);
        ResourceCategory category = (ResourceCategory) getIntent().getSerializableExtra("category");
        if (null != category) {
//            ResourceDetailBaseFragment1 detailFragment = ResourceDetailFactory.
//                    getDetailFragment(category.category);
            ResourceDetailContainerFragment detailFragment = new ResourceDetailContainerFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(ResourceManager.RESOURCE_CATEGORY, category);
            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.content,
                    detailFragment, ResourceManager.RESOURCE_CATEGORY).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == keyCode) {
            BaseFragment fragment = (BaseFragment) getSupportFragmentManager().
                    findFragmentByTag(ResourceManager.RESOURCE_CATEGORY);
            if (null != fragment && fragment.onBackEvent()) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
