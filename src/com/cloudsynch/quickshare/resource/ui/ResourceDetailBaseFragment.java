package com.cloudsynch.quickshare.resource.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.QuickShareApplication;
import com.cloudsynch.quickshare.resource.ResourceManager;
import com.cloudsynch.quickshare.resource.logic.IMultipleSelection;
import com.cloudsynch.quickshare.resource.module.ResourceCategory;
import com.cloudsynch.quickshare.utils.thumbnail.ImageWorker;

/**
 * Created by Xiaohu on 13-6-5.
 */
public abstract class ResourceDetailBaseFragment extends BaseFragment implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, View.OnClickListener {

    private final int SELECT_ALL = 0;
    public IMultipleSelection mMultipleSelection;
    protected ResourceCategory mCategory;
    protected GridView mGridView;
    protected IOnItemClick mOnItemClick;
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SELECT_ALL:
                    if (null != mOnItemClick) {
                        mOnItemClick.onSelectAll();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private AdapterView.OnItemLongClickListener mLongClickListener;
    private boolean isMultipleSelection;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGridView = new GridView(getActivity());
        mGridView.setNumColumns(3);
        mGridView.setHorizontalSpacing(20);
        mGridView.setVerticalSpacing(25);

        mCategory = (ResourceCategory) getArguments().getSerializable(ResourceManager.RESOURCE_CATEGORY);
        if (null != mCategory) {
            mGridView.setOnItemClickListener(this);
            mGridView.setOnItemLongClickListener(mLongClickListener);
            ImageWorker worker = ((QuickShareApplication) getActivity().getApplicationContext())
                    .getImageWorker();
            mGridView.setOnScrollListener(worker.getScrollerListener());

            initView(mGridView);
            mMultipleSelection = setMultipleSelection();
            if (isMultipleSelection) {
                mMultipleSelection.startMultipleSelect();
            }
        }

        return mGridView;
    }

    public ResourceCategory.Category getCategory() {
        return mCategory.category;
    }

    public abstract void initView(View view);

    public abstract IMultipleSelection setMultipleSelection();

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener longClickListener) {
        mLongClickListener = longClickListener;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!mMultipleSelection.isMultipleSelectMode()) {
            mMultipleSelection.startMultipleSelect();
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }

    @Override
    public boolean onBackEvent() {
        if (mMultipleSelection.isMultipleSelectMode()) {
            mMultipleSelection.cancelMultipleSelect();
            return true;
        }

        return false;
    }

    public void setMultipleSelectMode(boolean selectMode) {
        isMultipleSelection = selectMode;
    }

    public void setIOnItemClick(IOnItemClick iOnItemClick) {
        mOnItemClick = iOnItemClick;
    }


    public interface IOnItemClick {
        void onClick();

        void onSelectAll();
    }


}
