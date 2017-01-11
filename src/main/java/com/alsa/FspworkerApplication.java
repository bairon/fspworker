package com.alsa;

import com.alsa.worker.PrntscrSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class FspworkerApplication {

	@Autowired
	PrntscrSearch prntscrSearch;

	public static void main(String[] args) {
		SpringApplication.run(FspworkerApplication.class, args);
	}

	@PostConstruct
	public void init() {
		new Thread(prntscrSearch).start();
	}
}
