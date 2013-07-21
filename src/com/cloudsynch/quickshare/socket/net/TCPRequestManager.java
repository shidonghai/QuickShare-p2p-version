package com.cloudsynch.quickshare.socket.net;

import java.util.LinkedList;

//change to single instance if necessary in real project
public class TCPRequestManager extends RequestManager {
	private int RUNNING_THREAD_NUM = 5;
	private LinkedList<TCPSocketRequest> mReqList = null;
	private LinkedList<Executer> mExecList = null;

	public TCPRequestManager() {
		mReqList = new LinkedList<TCPSocketRequest>();
		mExecList = new LinkedList<Executer>();
	}

	public void startProcess() {
		// create all the working thread
		for (int i = 0; i < RUNNING_THREAD_NUM; i++) {
			Executer exec = new Executer(this);
			exec.start();

			mExecList.addFirst(exec);
		}
	}

	public void stopProcess() {
		// clear requestList
		// clear executer list
		// stop all the thread
	}

	public synchronized void Request(TCPSocketRequest req) {
		mReqList.addFirst(req);

		// notify one executer to work if all are in waiting states
		for (Executer exec : mExecList) {
			if (!exec.isRunning()) {
				synchronized (exec) {
					exec.notify();
				}
			}

		}
	}

	public synchronized SocketRequest removeRequest() {
		if (mReqList.size() != 0) {
			return mReqList.removeLast();
		}

		return null;
	}

	private class Executer extends Thread {
		SocketRequest mRequest = null;
		TCPRequestManager mReqManager = null;
		boolean mRunning = false;
		boolean mStopFlag = false;

		public Executer(TCPRequestManager mgr) {
			mReqManager = mgr;
		}

		public boolean isRunning() {
			return mRunning;
		}

		public void stopExec() {
			mStopFlag = true;
		}

		@Override
		public void run() {
			try {
				while (!mStopFlag) {
					// fetch request from list
					mRequest = mReqManager.removeRequest();
					if (mRequest != null) {
						mRunning = true;
						mRequest.startRequst();
					} else {
						mRunning = false;

						synchronized (this) {
							// wait for new request come
							this.wait();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
