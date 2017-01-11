package com.alsa;

import com.alsa.worker.Pinger;
import com.alsa.worker.PrntscrSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;

import static com.alsa.WebConstants.HOUR;
import static com.alsa.WebConstants.MINUTE;

@SpringBootApplication
public class FspworkerApplication {

	@Autowired
	PrntscrSearch prntscrSearch;

	@Autowired
	Pinger pinger;


	public static void main(String[] args) {
		SpringApplication.run(FspworkerApplication.class, args);
	}

	@PostConstruct
	public void init() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					pinger.ping();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, MINUTE * 5, MINUTE * 10);

		new Thread(prntscrSearch).start();
	}
}
