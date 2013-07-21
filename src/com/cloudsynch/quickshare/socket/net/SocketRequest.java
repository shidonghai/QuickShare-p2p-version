package com.cloudsynch.quickshare.socket.net;

public abstract class SocketRequest
{
    abstract void startRequst();
    
    protected static int STATE_PENDING = 0;
    protected static int STATE_ACTIVE = 1;
    protected static int STATE_FINISHED = 2;
    protected int mReqState = STATE_PENDING;
    
    protected long mReqId = 0;
    protected int mTimeout = 0;
    
    
    public void setRequestID(long id)
    {
        mReqId = id;    
    }
    
    public void setTimeout(int t)
    {
        mTimeout = t;
    }
}
