package com.alsa.worker;

/**
 * Created by alsa on 21.11.2016.
 */
public class WorkerException extends RuntimeException {
    public WorkerException() {
    }

    public WorkerException(String message) {
        super(message);
    }

    public WorkerException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkerException(Throwable e) {

    }

}
