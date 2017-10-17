package org.gareiss.mike.ramoc.tcp;

/**
 * Created by drue on 23.02.17.
 */

public interface TCPConnectionListener
{
    public void onMessage(String str);
    public void onError(int errorCode);
    public void onError(Exception ex);
}
