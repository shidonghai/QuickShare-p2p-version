package com.cloudsynch.quickshare;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudsynch.quickshare.utils.LogUtil;

/**
 * Basic Fragment, all the fragments started from main menu should extend this
 * fragment.
 * 
 * @author KingBright
 * 
 */
public abstract class BaseFragment extends Fragment {
	protected ViewGroup mViewGroup;
	private MainActivity mActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getActivity() instanceof MainActivity) {
			mActivity = (MainActivity) getActivity();
		}
		LogUtil.e("BaseFragment", this.getClass().getName());
	}

	public ViewGroup getContentView() {
		return mViewGroup;
	}

	@Override
	public final View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		if (mViewGroup == null) {
			mViewGroup = (ViewGroup) createView(inflater, container,
					savedInstanceState);
		} else {
			if (mViewGroup.getParent() != null) {
				((ViewGroup) mViewGroup.getParent()).removeView(mViewGroup);

			}
		}
		return mViewGroup;
	}

	public abstract View createView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState);

	public void addIgnoreView(View view) {
		if (mActivity != null) {
			mActivity.addIgnoreView(view);
		}
	}

	public void removeIgnoreView(View view) {
		if (mActivity != null) {
			mActivity.removeIgnoreView(view);
		}
	}

	public void showFragment(Fragment fragment) {
		getActivity().getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.addToBackStack(null).commit();
	}

	public void showMenu() {
		if (mActivity != null) {
			mActivity.showMenu();
		}
	}

	public boolean onBackEvent() {
		return false;
	}

}
