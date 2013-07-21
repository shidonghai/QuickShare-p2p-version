package com.cloudsynch.quickshare.socket.net;

import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Calendar;
import java.util.TimeZone;

/** 
 * SocketUtils 
 * <p>Description: </p> 
 * <p>Date: 2013-3-29</p> 
 * @author Shi Donghai 
 */ 

public class SocketUtils
{
    private static int lastAssignedPort = somewhatRandomPort();
    
    /**
     * Returns 1 free ports to be used.
     */
    public static synchronized int getFreeUDPPort()
    {
        return getFreeUPDPorts(1)[0];
    }

    /**
     * Returns the specified number of free ports to be used.
     */
    public static synchronized int[] getFreeUPDPorts(int num)
    {
        if (num <= 0)
        {
            throw new IllegalArgumentException("Invalid ports number: " + num);
        }
        
        DatagramSocket[] dss = new DatagramSocket[num];
        int[] ports = new int[num];

        try
        {
            for (int i = 0; i < num; ++i)
            {
                dss[i] = new DatagramSocket(0);
                ports[i] = dss[i].getLocalPort();
            }
        } 
        catch (Exception ex)
        {
            throw new Error("Unable to get " + num + " ports for UDP: " + ex);
        }
        finally
        {
            for (int i = 0; i < num; ++i)
            {
                if (dss[i] != null)
                {
                    dss[i].close();
                }
            }
        }
        
        return ports;
    }
    
    
    public static synchronized int getFreeTCPPort() 
    {
        try {
            ServerSocket ss = new ServerSocket(0);
            int port = ss.getLocalPort();

            ss.close();
            return port;
        } catch (Exception ex) {

        }
            
        return getNextPort_unsafe();
    }
    
    public static synchronized int getNextPort_unsafe() {
        if (++lastAssignedPort > 65534) {
            lastAssignedPort = 6000;
        }
        return lastAssignedPort;
    }

    /*
      * Returns a different port number every 6 seconds or so. The port number
      * should be about += 100 at each 6 second interval
      */
    private static int somewhatRandomPort() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int minutes = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);

        return 6000 + (1000 * minutes) + ((seconds / 6) * 100);
    }
}
