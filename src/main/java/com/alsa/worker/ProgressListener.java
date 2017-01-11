package com.alsa.worker;

/**
 * Created by alsa on 21.11.2016.
 */
public interface ProgressListener {
    void update(int current, int total);

}
