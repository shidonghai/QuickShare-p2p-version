package com.cloudsynch.quickshare.transport;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.socket.SocketService;

public class TransportActivity extends FragmentActivity {

	private final String TRANSPORT_FRAGMENT = "transport_fragment";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.transport_container);

		TransportFragment fragment = new TransportFragment();
		fragment.setArguments(getIntent().getBundleExtra("extra"));

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, fragment, TRANSPORT_FRAGMENT)
				.commitAllowingStateLoss();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String action = intent.getAction();
		if (SocketService.ACTION_RECEIVE.equals(action)) {
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(
					TRANSPORT_FRAGMENT);
			if (null != fragment && fragment instanceof TransportFragment) {
				TransportFragment transportFragment = (TransportFragment) fragment;
				transportFragment.showReceive();
			}
		}
	}
}
