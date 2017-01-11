package com.alsa.worker;

import com.alsa.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by alsa on 21.11.2016.
 */
@Component
public class PrntscrSearch implements Runnable {

    @Autowired
    Worker worker;

    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        worker.init("prnt.sc", new ProgressListener() {
            public void update(int current, int total) {
                if (current == 0 || current == 35) {
                    System.out.println("" + current + " / " + total);
                }
            }
        });
        while (true) {
            try {
                worker.processBlock();
            } catch (WorkerException we) {
                System.out.println("Сервис временно недоступен");
            } catch (Throwable t) {
                System.out.println("Непредвиденная ошибка ");
                t.printStackTrace();
            }
        }

    }
}
