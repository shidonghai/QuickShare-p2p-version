package com.cloudsynch.quickshare.socket.model;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageFactory {
	// create an message from json string
	public static SMessage createMessage(String jsonStr) {
		try {
			JSONObject object = new JSONObject(jsonStr);
			if (object.has("message")) {
				int type = object.optInt("message");

				SMessage msg = createMessage(type);

				if (object.has("data")) {
					msg.setData(object.optString("data"));
				}

				return msg;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static Class<?> getType(int type) {
		switch (type) {
		case SMessage.MSG_DISCOVER:
			return DiscoveryMessage.class;

		case SMessage.MSG_FILE_TRANSFER:
			return FileTransferMessage.class;

		case SMessage.MSG_HEART_BEAT:
			// msg = new HeartBeatMessage();
			break;

		case SMessage.MSG_STATUS_UPDATE:
			return StatusUpdateMessage.class;
		}
		return null;
	}

	// create an empty message
	public static SMessage createMessage(int type) {
		SMessage msg = null;
		switch (type) {
		case SMessage.MSG_DISCOVER:
			msg = new DiscoveryMessage();
			break;

		case SMessage.MSG_FILE_TRANSFER:
			msg = new FileTransferMessage();
			break;

		case SMessage.MSG_HEART_BEAT:
			// msg = new HeartBeatMessage();
			break;

		case SMessage.MSG_STATUS_UPDATE:
			msg = new StatusUpdateMessage();
			break;

		default:
			// unknown message type
		}

		return msg;
	}
}
