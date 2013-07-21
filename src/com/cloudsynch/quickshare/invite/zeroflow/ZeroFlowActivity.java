package com.cloudsynch.quickshare.invite.zeroflow;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import com.cloudsynch.quickshare.qrcode.QRCodeEncoder;
import com.cloudsynch.quickshare.qrcode.WriterException;
import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.socket.SocketRemoteController;
import com.cloudsynch.quickshare.socket.promp.PromptDialog;
import com.cloudsynch.quickshare.widgets.Titlebar;

/**
 * Created by Xiaohu on 13-6-29.
 */
public class ZeroFlowActivity extends Activity implements
		Titlebar.TitlebarClickListener {

	public static final int ZERO_FLOW_SOCKET_PORT = 2999;
	private View mInviteView;
	private View mWaitingView;
	private TextView mName;
	private TextView mIP;
	private View mFirstWay;
	private View mSecondWay;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PromptDialog.EVENT_CREATED:
				mWaitingView.setVisibility(View.GONE);
				mInviteView.setVisibility(View.VISIBLE);
				mFirstWay.setVisibility(View.VISIBLE);
				mSecondWay.setVisibility(View.VISIBLE);

				String name = (String) msg.obj;
				mName.setText(name);
				String ip = getLocalIpAddress() + ":" + ZERO_FLOW_SOCKET_PORT;
				mIP.setText(ip);
				setQRCode(ip);

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(),
						ServerSocketServer.class);
				startService(intent);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zero_flow_layout);

		Titlebar titlebar = (Titlebar) findViewById(R.id.title_bar);
		titlebar.setTitle(R.string.invite_zero);
		titlebar.setLeftImage(R.drawable.return_button);
		titlebar.setTitlebarClickListener(this);

		mInviteView = findViewById(R.id.invite_layout);
		mWaitingView = findViewById(R.id.waiting_layout);
		mName = (TextView) findViewById(R.id.wlan_name);
		mIP = (TextView) findViewById(R.id.ip_address);
		mFirstWay = findViewById(R.id.first_way);
		mSecondWay = findViewById(R.id.second_way);

		SocketRemoteController controller = SocketRemoteController
				.getMyInstance(this);
		controller.create(mHandler);
	}

	private void setQRCode(String qrCode) {
		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		int smallerDimension = width < height ? width : height;
		smallerDimension = smallerDimension * 5 / 10;

		Log.d("zxh", "smallerDimension:" + smallerDimension);

		try {
			QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(this, qrCode,
					"TEXT_TYPE", smallerDimension, false);
			Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
			ImageView qr = (ImageView) findViewById(R.id.qrcode);
			qr.setImageBitmap(bitmap);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent();
		intent.setClass(this, ServerSocketServer.class);
		stopService(intent);
	}

	@Override
	public void onLeftClick() {
		finish();
	}

	@Override
	public void onRightClick() {

	}

	public String getLocalIpAddress() {
		try {
			String ipv4;
			ArrayList<NetworkInterface> mylist = Collections
					.list(NetworkInterface.getNetworkInterfaces());

			for (NetworkInterface ni : mylist) {

				ArrayList<InetAddress> ialist = Collections.list(ni
						.getInetAddresses());
				for (InetAddress address : ialist) {
					if (!address.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(ipv4 = address
									.getHostAddress())) {
						return ipv4;
					}
				}

			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
