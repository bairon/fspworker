package com.alsa.domain;

public class Entry {
    public long id;
    public String prntscr;
    public String url;
    public long timestamp;

    public Entry() {
    }

    public Entry(String prntscr, String url) {
        this.prntscr = prntscr;
        this.url = url;
    }
}
