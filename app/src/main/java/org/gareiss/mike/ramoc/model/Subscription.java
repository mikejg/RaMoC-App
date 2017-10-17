package org.gareiss.mike.ramoc.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author john-tornblom
 */
public class Subscription {

    public long id;
    public String status;
    public List<Stream> streams = new ArrayList<Stream>();

    public long packetCount;
    public long queSize;
    public long delay;
    public long droppedBFrames;
    public long droppedIFrames;
    public long droppedPFrames;
}
