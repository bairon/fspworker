package com.alsa.worker;

import com.alsa.WebConstants;
import com.alsa.domain.Block;
import com.alsa.domain.Entry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alsa on 21.11.2016.
 */
@Component
public class Worker {
    public HttpClient client;
    public String prntscrServer;
    public BufferedImage ethalone;
    public ProgressListener progressListener;
    public RequestConfig requestConfig;
    CredentialsProvider credsProvider;
    String storeServer = "prntfsp.herokuapp.com";
    private static final Gson GSON = new GsonBuilder().create();

    public Worker() {
    }

    public void init(String prntscrServer, ProgressListener progressListener) {
        try {
            this.prntscrServer = prntscrServer;
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(AuthScope.ANY),
                    new UsernamePasswordCredentials("user", "user"));
            requestConfig = RequestConfig.custom().
                    setConnectionRequestTimeout(3000).setConnectTimeout(3000).setSocketTimeout(3000).build();
            client = HttpClientBuilder.create().addInterceptorLast(new RequestAcceptEncoding()).addInterceptorLast(new ResponseContentEncoding()).setDefaultRequestConfig(requestConfig).setDefaultCredentialsProvider(credsProvider).build();
            this.ethalone = ImageIO.read(getClass().getClassLoader().getResourceAsStream("static/img/bar.png"));
            this.progressListener = progressListener;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void processBlock() {
        try {
            Block block = requestBlock();
            List<String> repeatList = new ArrayList<>();
            for (int i = 0; i < 36; ++i) {
                String prntscr = block.base + Integer.toString(i, 36);
                try {
                    long startT = System.currentTimeMillis();
                    Entry entry = processEntry(prntscr, 10);
                    updateProgress(i, 36);
                    if (entry != null) {
                        postEntry(entry);
                    }
                    long endT = System.currentTimeMillis();
                    long diff = endT - startT;
                    long toSleep = 100 - diff;
                    if (toSleep > 0) {
                        sleep(toSleep);
                    }
                } catch (NotExistException nee) {
                    repeatList.add(prntscr);
                }
            }
            for (String prntscr : repeatList) {
                Entry entry = processEntry(prntscr, 1);
                if (entry != null) {
                    postEntry(entry);
                }
            }
            postBlock(block);
        } catch (Throwable t) {
            System.out.print("Ошибка ");
            t.printStackTrace();
            sleep(5000);
        }
    }

    private void updateProgress(int current, int total){
        if (progressListener != null) {
            progressListener.update(current, total);
        }
    }

    private Entry processEntry(String prntscr, int repeat) throws NotExistException  {
        int removedscreeninarow = 0;
        for (int i = 0; i < repeat; ++i) {
            try {
                String url = "http://" + prntscrServer + "/" + prntscr + (removedscreeninarow > 0 ? "?ts=" + System.currentTimeMillis() : "");
                System.out.print("Visiting " + url);
                String between = request(client, url);
                if (between == null) continue;
                System.out.println(" " + between);
                if (between.contains("8tdUI8N.png") ||
                        between.equals("https://img-fotki.yandex.ru/get/49649/5191850.0/0_173a7b_211be8ff_orig")) {
                    removedscreeninarow++;
                    if (removedscreeninarow <= 10) {
                        sleep(5000);
                        continue;
                    } else {
                        throw new NotExistException();
                    }
                }
                BufferedImage image = null;
                HttpResponse execute = client.execute(new HttpGet(between));
                if (execute == null) continue;
                HttpEntity entity = execute.getEntity();
                if (entity == null) continue;
                InputStream is = entity.getContent();
                if (is == null) continue;
                try {
                    image = ImageIO.read(is);
                } catch (Throwable t) {
                    t.printStackTrace();
                    return null;
                } finally {
                    is.close();
                }
                if (image.getWidth() > 600 || image.getHeight() > 1200) {
                    image = null;
                    return null;
                }
                boolean found = findSubimage(image, ethalone);
                if (found) {
                    Entry entry = new Entry();
                    entry.prntscr = prntscr;
                    entry.url = between;
                    image = null;
                    return entry;
                }
                image = null;
                return null;
            } catch (HttpResponseException hre) {
                System.out.println(" " + hre.getMessage());
                sleep(1500);
            } catch (Throwable t) {
                System.out.println(" " + t.getMessage());
                //client = HttpClientBuilder.create().addInterceptorLast(new RequestAcceptEncoding()).addInterceptorLast(new ResponseContentEncoding()).setDefaultRequestConfig(requestConfig).build();
                if (t.getMessage() != null && !t.getMessage().contains("Error reading PNG")) {
                    sleep(1500);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private String request(HttpClient client, String url) throws IOException {
        HttpGet request = new HttpGet(url);
        String response = client.execute(request, new BasicResponseHandler());
        String between = between(response, "meta name=\"twitter:image:src\" content=\"", "\"");
        if (between.length() > 150 || between.length() < 0) {
            System.out.println(response);
            sleep(10000);
            return null;

        }
        return between;
    }

    public String between(final String source, final String starttoken, final String endtoken) {
        int start = starttoken == null ? 0 : source.indexOf(starttoken);
        int end = endtoken == null ? source.length() : source.indexOf(endtoken, start + (starttoken == null ? 0 : starttoken.length()));
        return source.substring(start + (starttoken == null ? 0 : starttoken.length()), end);
    }
    private void postEntry(Entry entry) {
        HttpPost postEntry = new HttpPost("http://" + storeServer + WebConstants.POST_ENTRY);
        try {
            StringEntity entity = new StringEntity(GSON.toJson(entry));
            entity.setContentType("application/json");
            postEntry.setEntity(entity);
            String response = client.execute(postEntry, new BasicResponseHandler());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void postBlock(Block block) {
        HttpPost postBlock = new HttpPost("http://" + storeServer + WebConstants.POST_BLOCK);
        try {
            StringEntity entity = new StringEntity(GSON.toJson(block));
            entity.setContentType("application/json");
            postBlock.setEntity(entity);
            String response = client.execute(postBlock, new BasicResponseHandler());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Block requestBlock() throws IOException {
        HttpGet request = new HttpGet("http://" + storeServer + WebConstants.CREATE_BLOCK);
        String response = client.execute(request, new BasicResponseHandler());
        if (response != null && response.length() > 0) {
            return GSON.fromJson(response, Block.class);
        }
        return null;
    }

    /**
     * Finds the a region in one image that best matches another, smaller, image.
     */
    public static boolean findSubimage(BufferedImage im1, BufferedImage im2) {
        int w1 = im1.getWidth();
        int h1 = im1.getHeight();
        int w2 = im2.getWidth();
        int h2 = im2.getHeight();
        assert (w2 <= w1 && h2 <= h1);
        for (int x = 0; x < Math.min(w1 - w2, 100); x++) {
            for (int y = 0; y < h1 - h2; y++) {
                boolean equal = compareImages(im1, x, y, w2, h2, im2);
                if (equal) return true;
            }
        }
        return false;
    }

    /**
     * Determines how different two identically sized regions are.
     */
    public static boolean compareImages(BufferedImage im1, int X, int Y, int w2, int h2, BufferedImage im2) {
        for (int x = 0; x < w2; x++) {
            for (int y = 0; y < h2; y++) {
                boolean equal = equalARGB(im1.getRGB(x + X, y + Y), im2.getRGB(x, y));
                if (!equal) return false;
            }
        }
        return true;
    }

    /**
     * Calculates the difference between two ARGB colours (BufferedImage.TYPE_INT_ARGB).
     */
    public static boolean equalARGB(int rgb1, int rgb2) {
        double r1 = ((rgb1 >> 16) & 0xFF);
        double r2 = ((rgb2 >> 16) & 0xFF);
        double g1 = ((rgb1 >> 8) & 0xFF);
        double g2 = ((rgb2 >> 8) & 0xFF);
        double b1 = (rgb1 & 0xFF);
        double b2 = (rgb2 & 0xFF);
        return (r1 == r2 && g1 == g2 && b1 == b2 || (r2 == 0 && g2 == 0 && b2 == 0));
    }

    private static void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
        }

    }
}
