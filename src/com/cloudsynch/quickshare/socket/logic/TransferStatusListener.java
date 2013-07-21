package com.cloudsynch.quickshare.socket.logic;

public abstract class TransferStatusListener
{
    public static final int MSG_TRANSFER_START = 0x0001;
    public static final int MSG_TRANSFER_FINISH = 0x0002;
    public static final int MSG_TRANSFER_ERROR = 0x00004;
    public static final int MSG_TRANSFER_PRORESS = 0x0008;
    public static final int MSG_REQUEST_STATE = 0x00010;
    
    public abstract void onEvent(long reqId, int arg1);
}
