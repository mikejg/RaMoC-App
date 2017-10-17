package org.gareiss.mike.ramoc.model;
/**
 *
 * @author john-tornblom
 */
public class HttpTicket {
    public final String path;
    public final String ticket;

    public HttpTicket(String path, String ticket) {
        this.path = path;
        this.ticket = ticket;
    }
}

