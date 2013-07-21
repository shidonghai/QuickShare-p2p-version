package com.cloudsynch.quickshare.socket.logic;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

import com.cloudsynch.quickshare.socket.connection.ConnectionManager;
import com.cloudsynch.quickshare.socket.model.MessageFactory;
import com.cloudsynch.quickshare.socket.model.SMessage;
import com.cloudsynch.quickshare.utils.LogUtil;
import com.google.gson.Gson;

public class MessageCenter {
	private static final int MSG_RECEIVED = 0;
	private static final int MSG_SEND_FINISHED = 1;
	private static final int FILE_TRANSFER_STARTED = 2;
	private static final int FILE_TRANSFER_FINISHED = 3;
	private static final int FILE_TRANSFER_PROGRESS_UPDATED = 4;
	private static final int FILE_TRANSFER_ERROR = 5;

	private ConnectionManager mConnectionManager = null;
	private static MessageCenter mEventCenter = null;
	private List<MessageListener> mListenerList = new ArrayList<MessageListener>();

	private MessageCenter() {
		// TODO Auto-generated constructor stub
	}

	public static MessageCenter getInstance() {
		if (mEventCenter == null) {
			mEventCenter = new MessageCenter();
			mEventCenter.init();
		}

		return mEventCenter;
	}

	public void addMessageListener(MessageListener listener) {
		mListenerList.add(listener);
	}

	// in order not to block the socket process, we should not handle the
	// message in the callback function, instead, we use a handler to handle the
	// message off line.
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			// new message received from SIGNAL_PORT
			case MSG_RECEIVED: {
				SMessage rcvMsg = (SMessage) msg.obj;

				rcvMsg.dump();

				switch (rcvMsg.getMessageType()) {
				case SMessage.MSG_DISCOVER: {
					int type = 0;

					if (rcvMsg.getMsgDirection() == SMessage.REQ) {
						type = MessageListener.MSG_USER_DISCOVER_REQ;
					} else {
						type = MessageListener.MSG_USER_DISCOVER_ACK;
					}

					dispatchEvent(type, rcvMsg);

					break;
				}

				case SMessage.MSG_STATUS_UPDATE: {
					dispatchEvent(MessageListener.MSG_USER_STATUS_UPDATE,
							rcvMsg);
					break;
				}
				case SMessage.MSG_FILE_TRANSFER: {
					if (rcvMsg.getMsgDirection() == SMessage.REQ) {
						dispatchEvent(MessageListener.MSG_TRANSFER_REQUEST,
								rcvMsg);
					} else {
						dispatchEvent(MessageListener.MSG_TRANSFER_CONFIRM,
								rcvMsg);
					}
					break;
				}

				default:
					break;
				}
				break;
			}
			// file transfer started
			case FILE_TRANSFER_STARTED: {

			}
			// file transfer finished
			case FILE_TRANSFER_FINISHED: {
				break;
			}
			// file transfer progress updated
			case FILE_TRANSFER_PROGRESS_UPDATED: {
				break;
			}
			// file transfer error occured
			case FILE_TRANSFER_ERROR: {
				break;
			}
			//
			case MSG_SEND_FINISHED: {
				break;
			}

			default:
				break;
			}
		};
	};

	public void init() {
		mConnectionManager = ConnectionManager.getInstance();

		// add callback
		mConnectionManager
				.setCallBack(new ConnectionManager.ConnectionCallback() {
					@Override
					public void onMessageSend(long reqId, boolean succeeded) {
						android.os.Message msg = new android.os.Message();
						msg.what = MSG_SEND_FINISHED;
						msg.obj = Long.valueOf(reqId);
						msg.arg1 = succeeded ? 1 : 0;

						mHandler.sendMessage(msg);
					}

					@Override
					public void onMessageReceived(String ip, int port,
							byte[] data) {
						try {
							// truck the of '0' in the data
							int balance = 0;
							StringBuilder builder = new StringBuilder();
							String str = new String(data, "UTF-8");
							for (int i = 0; i < str.length(); i++) {
								builder.append(str.charAt(i));
								if (str.charAt(i) == '{') {
									balance++;
									LogUtil.e("found", "{");
								} else if (str.charAt(i) == '}') {
									balance--;
									LogUtil.e("found", "}");
									if (balance == 0) {
										break;
									}
								}
							}

							String json = builder.toString();
							LogUtil.e("json", json);
							JSONObject jsonObj = new JSONObject(json);
							// TODO: define a constant final variant for all
							// these JSON key
							if (jsonObj.has("mMsgType")) {
								Class<?> cls = MessageFactory.getType(jsonObj
										.optInt("mMsgType"));
								Gson gson = new Gson();

								Object object = null;
								if (json.endsWith("}1}")) {
									object = gson.fromJson(json.substring(0,
											json.length() - 2), cls);
								} else if (json.endsWith("}}")) {
									object = gson.fromJson(json.substring(0,
											json.length() - 1), cls);
								} else {
									object = gson.fromJson(builder.toString(),
											cls);
								}

								SMessage msg = (SMessage) object;
								msg.setRemoteAdress(ip);
								msg.setRemotePort(port);
								if (msg != null) {
									android.os.Message.obtain(mHandler,
											MSG_RECEIVED, msg).sendToTarget();
								}

							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

					@Override
					public void onFileTransferStart(long reqId) {
						android.os.Message msg = new android.os.Message();
						msg.what = FILE_TRANSFER_STARTED;
						msg.obj = Long.valueOf(reqId);

						mHandler.sendMessage(msg);
					}

					@Override
					public void onFileTransferProgress(long reqId,
							int sizeTransferred) {
						android.os.Message msg = new android.os.Message();
						msg.what = FILE_TRANSFER_PROGRESS_UPDATED;
						msg.obj = Long.valueOf(reqId);
						msg.arg1 = sizeTransferred;

						mHandler.sendMessage(msg);
					}

					@Override
					public void onFileTransferFinished(long reqId) {
						android.os.Message msg = new android.os.Message();
						msg.what = FILE_TRANSFER_FINISHED;
						msg.obj = Long.valueOf(reqId);

						mHandler.sendMessage(msg);
					}

					@Override
					public void onFileTransferError(long reqId) {
						android.os.Message msg = new android.os.Message();
						msg.what = FILE_TRANSFER_ERROR;
						msg.obj = Long.valueOf(reqId);

						mHandler.sendMessage(msg);
					}
				});
	}

	private void dispatchEvent(int type, SMessage msg) {
		for (MessageListener listener : mListenerList) {
			if ((listener.mEventFilter & type) != 0) {
				listener.onEvent(type, msg);
			}
		}
	}

	public void destory() {
		// TODO
	}
}
