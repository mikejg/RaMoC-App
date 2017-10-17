package org.gareiss.mike.ramoc.model;

/**
 *
 * @author john-tornblom
 */
public class Packet {

    public Subscription subscription;
    public Stream stream;
    public int frametype;
    public long dts;
    public long pts;
    public long duration;
    public byte[] payload;
}

