package com.cloudsynch.quickshare.socket.promp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.cloudsynch.quickshare.R;

public class PromptActivity extends FragmentActivity {

	private PromptDialog mPromptDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = new LinearLayout(this);
		view.setId(R.id.container);
		setContentView(view);

		mPromptDialog = new PromptDialog(getIntent().getBundleExtra("extra"));
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.container, mPromptDialog).commit();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// when touch outside the dialog view, not exit
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mPromptDialog.onNewIntent(intent);
	}

	@Override
	public void onBackPressed() {
		// sometime we need to finish the previous activity when exit current
		// dialog view
		mPromptDialog.onBackPressed();
	}
}
