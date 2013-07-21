package com.cloudsynch.quickshare.resource.ui;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.resource.ResourceManager;
import com.cloudsynch.quickshare.resource.logic.IContentChanged;
import com.cloudsynch.quickshare.resource.module.ResourceCategory;
import com.cloudsynch.quickshare.utils.EventConstant;
import com.cloudsynch.quickshare.utils.EventManager;
import com.cloudsynch.quickshare.utils.FileUtil;
import com.cloudsynch.quickshare.utils.LogUtil;
import com.cloudsynch.quickshare.widgets.ResourcePercentageBar;
import com.cloudsynch.quickshare.widgets.Titlebar;
import com.cloudsynch.quickshare.widgets.Titlebar.TitlebarClickListener;

/**
 * Created by Xiaohu on 13-5-28.
 */
public class ResourceManagerFragment extends BaseFragment implements
        AdapterView.OnItemClickListener, TitlebarClickListener, IContentChanged {

    private final String TAG = "ResourceManagerFragment";

    private InitResourceTask mInitResourceTask;

    private ResourceManagerAdapter mCategoryAdapter;

    private ResourceCategorySizeAdapter mCategorySizeAdapter;

    private List<ResourcePercentageBar.Entry> mResourceEntry = new ArrayList<ResourcePercentageBar.Entry>();

    private long mSDSize;

    private ResourceCategory mOtherCategory;

    private ResourcePercentageBar mSDProgress;

    private ResourcePercentageBar mDataProgress;

    private View mSDProgressView;

    private Titlebar mTitlebar;

    private boolean mIsLoaded;

    private ResourceManager mResourceManager;

    private List<ResourceCategory> mResourceList;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.resource_manager, container, false);
        setUp(view);
        return view;
    }

    private void setUp(View view) {
        mTitlebar = (Titlebar) view.findViewById(R.id.title_bar);
        mTitlebar.setTitle(R.string.resource_manager_title);
        mTitlebar.setLeftImage(R.drawable.list_icon);
        mTitlebar.setRightImage(R.drawable.refresh_icon);
        mTitlebar.setTitlebarClickListener(this);

        mResourceList = new ArrayList<ResourceCategory>();
        for (ResourceCategory.Category category : ResourceCategory.Category
                .values()) {
            if (category != ResourceCategory.Category.OTHER) {
                mResourceList.add(new ResourceCategory(category));
            }
        }

        GridView gridView = (GridView) view.findViewById(R.id.grid_view);
        mCategoryAdapter = new ResourceManagerAdapter(mResourceList);
        gridView.setAdapter(mCategoryAdapter);
        gridView.setOnItemClickListener(this);

        GridView categorySize = (GridView) view
                .findViewById(R.id.category_size);
        mCategorySizeAdapter = new ResourceCategorySizeAdapter();
        categorySize.setAdapter(mCategorySizeAdapter);

        mSDProgressView = view.findViewById(R.id.sd_storage);
        initSDStorage(mSDProgressView, R.string.resource_manager_sd,
                Environment.getExternalStorageDirectory());

        initSDStorage(view.findViewById(R.id.data_storage),
                R.string.resource_manager_data, Environment.getDataDirectory());

        mSDSize = ResourceManager.getSDAllSize(Environment
                .getExternalStorageDirectory());
        View sd = view.findViewById(R.id.sd_storage);
        mSDProgress = (ResourcePercentageBar) sd.findViewById(R.id.progress);
        mDataProgress = (ResourcePercentageBar) view.findViewById(
                R.id.data_storage).findViewById(R.id.progress);
        mInitResourceTask = new InitResourceTask();
        mInitResourceTask.execute(mResourceList);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!mIsLoaded) {
            reLoad();
        }
    }


    private void initSDStorage(View view, int typeId, File path) {
        TextView available = (TextView) view.findViewById(R.id.sd_available);
        TextView used = (TextView) view.findViewById(R.id.sd_used);
        TextView allText = (TextView) view.findViewById(R.id.sd_all);

        ProgressBar progressBar = (ProgressBar) view
                .findViewById(R.id.sd_progress);
        progressBar.setProgress(0);
        TextView type = (TextView) view.findViewById(R.id.storage_type);
        type.setText(typeId);

        long all = ResourceManager.getSDAllSize(path);
        long ava = ResourceManager.getSDAvailableSize(path);
        DecimalFormat format = new DecimalFormat("0.00%");
        double progress = (double) (all - ava) / all;
        String result = format.format(progress);
        progressBar.setProgress((int) (progress * 100));

        available.setText(String.format(
                getString(R.string.resource_manager_available),
                FileUtil.formatFromByte(ava)));
        used.setText(String.format(getString(R.string.resource_manager_used),
                FileUtil.formatFromByte(all - ava)));
        allText.setText(String.format(getString(R.string.resource_manager_all),
                FileUtil.formatFromByte(all)));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResourceManager = ResourceManager.getInstance(getActivity());
        mResourceManager.registerListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        mSDProgressView.setVisibility(Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (null != mCategoryAdapter) {
            EventManager.getInstance().onEvent(EventConstant.RESOURCE_MANAGER_IDS[i]);

            Intent intent = new Intent();
            intent.setClass(getActivity(), ResourceDetailActivity.class);
            intent.putExtra("category", mCategoryAdapter.getItem(i));
            startActivity(intent);
        }
    }

    @Override
    public void onLeftClick() {
        showMenu();
    }

    @Override
    public void onRightClick() {
        reLoad();
    }

    private void reLoad() {
        if (null == mInitResourceTask) {
            mInitResourceTask = new InitResourceTask();
            mInitResourceTask.execute(mResourceList);
        }
    }

    @Override
    public void onContentChanged() {
        LogUtil.d(TAG, "onContentChanged");
        reLoad();
    }

    private class ResourceManagerAdapter extends BaseAdapter {

        private List<ResourceCategory> mResources;

        ResourceManagerAdapter(List<ResourceCategory> resources) {
            mResources = resources;
        }

        @Override
        public int getCount() {
            return mResources == null ? 0 : mResources.size();
        }

        @Override
        public ResourceCategory getItem(int i) {
            return mResources == null ? null : mResources.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (null == view) {
                view = View.inflate(getActivity(),
                        R.layout.resource_manager_grid, null);
            }
            TextView type = (TextView) view.findViewById(R.id.type);
            TextView count = (TextView) view.findViewById(R.id.count);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            ResourceCategory category = mResources.get(i);
            int icon = category.category.getIcon();
            iconView.setImageResource(icon);
            type.setText(category.category.getStringId());
            count.setText(String.format(
                    getString(R.string.resource_manager_count), category.count));
            return view;
        }

        public void notifyDataSetChanged(List<ResourceCategory> list) {
            if (null == list) {
                return;
            }
            mResources = list;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mInitResourceTask) {
            mInitResourceTask.cancel(true);
            mInitResourceTask = null;
        }

        mResourceManager.removeListener(this);

    }

    private class ResourceCategorySizeAdapter extends BaseAdapter {

        private List<ResourceCategory> mResources;

        @Override
        public int getCount() {
            return mResources == null ? 0 : mResources.size();
        }

        @Override
        public ResourceCategory getItem(int i) {
            return mResources == null ? null : mResources.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (null != mResources) {
                if (null == view) {
                    view = View.inflate(getActivity(),
                            R.layout.resource_manager_size, null);
                }

                ImageView color = (ImageView) view
                        .findViewById(R.id.category_color);
                TextView size = (TextView) view
                        .findViewById(R.id.category_size);

                ResourceCategory category = mResources.get(i);
                color.setImageResource(category.category.getImage());
                size.setText(getString(category.category.getStringId()) + ":"
                        + FileUtil.formatFromByte(category.size));

                return view;
            }
            return null;
        }

        public void notifyDataSetChanged(List<ResourceCategory> list) {
            if (null == list) {
                return;
            }
            mResources = list;
            notifyDataSetChanged();
        }
    }

    private class InitResourceTask extends
            AsyncTask<List<ResourceCategory>, Void, List<ResourceCategory>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mTitlebar.startRightAnimation(R.anim.rotate);
        }

        @Override
        protected List<ResourceCategory> doInBackground(
                List<ResourceCategory>... lists) {
            List<ResourceCategory> list = lists[0];
            mResourceEntry.clear();
            if (null == list) {
                return null;
            }
            long size = 0;
            for (int i = 0; i < list.size(); i++) {
                ResourceCategory category = list.get(i);
                mResourceManager.initResource(getActivity(), category);
                size += category.size;
                if (null != getActivity()) {
                    mResourceEntry.add(ResourcePercentageBar.createEntry(
                            (float) category.size / mSDSize, getResources()
                            .getColor(category.category.getColor())));
                }
            }
            long usedSize = mSDSize
                    - ResourceManager.getSDAvailableSize(Environment
                    .getExternalStorageDirectory());
            long otherSize = usedSize - size;
            mResourceEntry.add(ResourcePercentageBar.createEntry(
                    (float) otherSize / mSDSize, R.color.resource_other));
            mOtherCategory = new ResourceCategory(
                    ResourceCategory.Category.OTHER);
            mOtherCategory.size = otherSize;
            return list;
        }

        @Override
        protected void onPostExecute(List<ResourceCategory> resourceCategories) {
            super.onPostExecute(resourceCategories);
            if (null == resourceCategories || null == mCategoryAdapter) {
                return;
            }

            mIsLoaded = true;
            mInitResourceTask = null;
            mCategoryAdapter.notifyDataSetChanged(resourceCategories);
            List<ResourceCategory> size = new ArrayList<ResourceCategory>();
            size.addAll(resourceCategories);
            size.add(mOtherCategory);
            mCategorySizeAdapter.notifyDataSetChanged(size);

            mSDProgress.setEntries(mResourceEntry);
            mSDProgress.invalidate();

            long dataSize = ResourceManager.getSDAllSize(Environment
                    .getDataDirectory());
            long avaSize = ResourceManager.getSDAvailableSize(Environment
                    .getDataDirectory());
            List<ResourcePercentageBar.Entry> entries = new ArrayList<ResourcePercentageBar.Entry>();
            entries.add(ResourcePercentageBar.createEntry(
                    (float) (dataSize - avaSize) / dataSize,
                    R.color.resource_other));
            mDataProgress.setEntries(entries);
            mDataProgress.invalidate();

            mTitlebar.clearRightAnimation();
        }
    }

}
