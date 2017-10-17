package org.gareiss.mike.ramoc.tv.htsp;

/**
 * Created by drue on 11.03.17.
 */

public interface HTSConnectionListener
{
    public void onMessage(HTSMessage response);
    public void onError(int errorCode);
    public void onError(Exception ex);
}
