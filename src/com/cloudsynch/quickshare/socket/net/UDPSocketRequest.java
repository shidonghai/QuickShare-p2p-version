package com.cloudsynch.quickshare.socket.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.util.Log;

/**
 * UDPSocketRequest
 * <p>
 * Description:
 * </p>
 * <p>
 * Date: 2013-3-29
 * </p>
 * 
 * @author Shi Donghai
 */

public class UDPSocketRequest extends SocketRequest
{
    public static interface UDPRequestCallback
    {
        abstract void onSend(long reqId, boolean succeeded);
    }

    private UDPRequestCallback mCallback = null;
    private DatagramPacket mRequestPackage = null;
    private DatagramSocket mSocket = null;
    private int mPort;
    private String mIp;
    private byte[] mData = null;

    public UDPSocketRequest(int port, String addr)
    {
        mPort = port;
        mIp = addr;
    }

    public void setCallBack(UDPRequestCallback callback)
    {
        mCallback = callback;
    }

    public void setRequestData(byte[] data)
    {
        mData = data;
    }

    public int getReqState()
    {
        return mReqState;
    }

    @Override
    public void startRequst()
    {
        try
        {
            InetAddress addr = InetAddress.getByName(mIp);
            mRequestPackage = new DatagramPacket(mData, mData.length, addr, mPort);

            mReqState = STATE_ACTIVE;
            mSocket = new DatagramSocket();
            mSocket.setSoTimeout(mTimeout);
            mSocket.setBroadcast(true);

            mSocket.send(mRequestPackage);

            mCallback.onSend(mReqId, true);

            return;
        } catch (SocketException e)
        {
            Log.w("ShareApp", "socket send exception " + e.toString());
            e.printStackTrace();
        } catch (IOException e)
        {
            Log.w("ShareApp", "socket send exception " + e.toString());
            e.printStackTrace();
        } finally
        {
            mReqState = STATE_FINISHED;
            if (mSocket != null)
            {
                Log.w("ShareApp", "UDP socket closed ");
                mSocket.close();
            }
        }

        mCallback.onSend(mReqId, false);
    }
}
