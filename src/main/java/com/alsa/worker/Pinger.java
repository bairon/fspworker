package com.alsa.worker;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by alsa on 11.01.2017.
 */@Component
public class Pinger {
    public HttpClient client;
    public Pinger() {
        RequestConfig requestConfig = RequestConfig.custom().
                setConnectionRequestTimeout(3000).setConnectTimeout(3000).setSocketTimeout(3000).build();
        client = HttpClientBuilder.create().addInterceptorLast(new RequestAcceptEncoding()).addInterceptorLast(new ResponseContentEncoding()).setDefaultRequestConfig(requestConfig).build();
    }

    public void ping() {
        ping(1);
        ping(2);
        ping(3);
        ping(4);
     }

    private void ping(int number) {
        System.out.print("Pinging " + number);
        HttpGet ping = new HttpGet("https://fspworker" + number + ".herokuapp.com/ping");
        try {
            String result = client.execute(ping, new BasicResponseHandler());
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
