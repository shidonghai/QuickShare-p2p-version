package com.cloudsynch.quickshare.invite.zeroflow;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import com.cloudsynch.quickshare.utils.LogUtil;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpVersion;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;

/**
 * Created by Xiaohu on 13-6-29.
 */
public class ServerSocketServer extends Service {

    private final String TAG = "ServerSocketServer";

    private ServerSocketThread mServerThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "onCreate");

        mServerThread = new ServerSocketThread();
        mServerThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");

        if (null != mServerThread) {
            mServerThread.cancel = true;
        }
    }

    private String getAppFilePath() {
        String path = null;
        PackageManager manager = getPackageManager();
        try {
            ApplicationInfo info = manager.getApplicationInfo(getPackageName(), 0);
            path = info.sourceDir;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }

    private class ServerSocketThread extends Thread {

        boolean cancel;

        @Override
        public void run() {

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(ZeroFlowActivity.ZERO_FLOW_SOCKET_PORT);
                LogUtil.d(TAG, "start zero flow server socket");
                while (!cancel) {
                    Socket socket = serverSocket.accept();
                    socket.setSoTimeout(5000);
                    LogUtil.d(TAG, "zero flow server socket accept");

                    DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();
                    HttpParams httpParams = new BasicHttpParams();
                    serverConnection.bind(socket, httpParams);
                    new WorkingThread(serverConnection).start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class WorkingThread extends Thread {
        HttpServerConnection mServer;

        WorkingThread(HttpServerConnection connection) {
            mServer = connection;
        }

        @Override
        public void run() {
            try {
                PackageManager manager = getPackageManager();
                ApplicationInfo info = manager.getApplicationInfo(getPackageName(), 0);
                File file = new File(info.sourceDir);

                if (file.exists()) {
                    String fileName = URLEncoder.encode(manager.getApplicationLabel(info).toString(), "utf-8") + ".apk";
                    LogUtil.d(TAG, "file exists:" + file.exists() + "  " + fileName);

                    HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
                    response.setHeader("Content-Type", "application/octet-stream");
                    response.setHeader("Content-Length", "" + file.length());
                    response.setHeader("Content-disposition", "attachment;filename=" + fileName);
                    FileEntity fileEntity = new FileEntity(file, "application/octet-stream");
                    mServer.sendResponseHeader(response);
                    response.setEntity(fileEntity);
                    mServer.sendResponseEntity(response);
                }
            } catch (HttpException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
//                try {
//                    mServer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }
}
