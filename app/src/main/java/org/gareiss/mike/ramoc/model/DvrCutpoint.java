package org.gareiss.mike.ramoc.model;

public class DvrCutpoint
{
    // start and stop are in ms
    public int start;
    public int end;

    // 0=Cut, 1=Mute, 2=Scene, 3=Commercial break
    public int type;
}
