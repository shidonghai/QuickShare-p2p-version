package com.cloudsynch.quickshare.socket.net;

import java.util.LinkedList;

public class UDPRequestManager extends RequestManager
{
    private int RUNNING_THREAD_NUM = 10;
    private LinkedList<UDPSocketRequest> mReqList = null;
    private LinkedList<Executer> mExecList = null;

    public UDPRequestManager()
    {
        mReqList = new LinkedList<UDPSocketRequest>();
        mExecList = new LinkedList<Executer>();
    }

    public void startProcess()
    {
        // create all the working thread
        for (int i = 0; i < RUNNING_THREAD_NUM; i++)
        {
            Executer exec = new Executer(this);
            exec.start();

            mExecList.addFirst(exec);
        }
    }

    public void stopProcess()
    {

    }

    public synchronized void Request(UDPSocketRequest req)
    {
        mReqList.addFirst(req);

        // notify one executer to work if all are in waiting states
        for (Executer exec : mExecList)
        {
            if (!exec.isRunning())
            {
                synchronized (exec)
                {
                    exec.notify();
                }
            }

        }
    }

    public synchronized UDPSocketRequest removeRequest()
    {
        if (mReqList.size() != 0)
        {
            return mReqList.removeLast();
        }

        return null;
    }

    private class Executer extends Thread
    {
        UDPSocketRequest mRequest = null;
        UDPRequestManager mReqManager = null;
        boolean mRunning = false;
        boolean mStopFlag = false;

        public Executer(UDPRequestManager mgr)
        {
            mReqManager = mgr;
        }

        public boolean isRunning()
        {
            return mRunning;
        }

        @Override
        public void run()
        {
            try
            {
                while (!mStopFlag)
                {
                    // fetch request from list
                    mRequest = mReqManager.removeRequest();
                    if (mRequest != null)
                    {
                        mRunning = true;
                        mRequest.startRequst();
                    } 
                    else
                    {
                        mRunning = false;
                        
                        synchronized (this)
                        {
                            // wait for new request come
                            wait();
                        }
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }
}
