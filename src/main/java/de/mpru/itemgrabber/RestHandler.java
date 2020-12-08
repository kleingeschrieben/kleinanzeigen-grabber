package de.mpru.itemgrabber;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestHandler {
    private OkHttpClient client;
    private static final Logger logger = LoggerFactory.getLogger(RestHandler.class);

    public RestHandler() {
        client = new OkHttpClient().newBuilder().build();
    }

    public String sendGetRequest(String url) {
        String body = "";
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();
        try {
            Response response = client.newCall(request).execute();
            body = response.body().string();
        } catch (Exception e) {
            logger.error("Error while sending GET request to: {}", url, e);
        }

        return body;
    }

}
