package net.dengzixu.blivedanmaku.utils;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtil {
    private final HttpClient httpClient;

    public HttpUtil(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpUtil() {
        this(HttpClient.newBuilder().build());
    }

    public <T> HttpResponse<T> sendRequest(URI uri, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();

        return httpClient.send(httpRequest, responseBodyHandler);
    }

    public HttpResponse<String> sendRequest(URI uri)
            throws IOException, InterruptedException {
        return sendRequest(uri, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> sendRequest(URI uri, String cookies) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .setHeader("Cookie", cookies)
                .GET()
                .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> sendRequest(URI uri, URI cookieURI, Map<String, String> cookies)
            throws IOException, InterruptedException {
        CookieManager cookieManager = new CookieManager();
        CookieStore cookieStore = cookieManager.getCookieStore();

        cookies.forEach((k, v) -> {
            HttpCookie httpCookie = new HttpCookie(k, v);
            httpCookie.setPath("/");
            cookieStore.add(cookieURI, httpCookie);
        });

        HttpClient httpClientWithCookie = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();

        HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                .setHeader("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();

        return httpClientWithCookie.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

    }

    public <T> HttpResponse<T> sendRequest(String stringURI, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException, URISyntaxException {
        URI uri = new URI(stringURI);

        return this.sendRequest(uri, responseBodyHandler);
    }


}
