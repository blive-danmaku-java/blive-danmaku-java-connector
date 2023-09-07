package net.dengzixu.blivedanmaku.api.bilibili.live;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;

import java.io.IOException;

public class BiliAPI {
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(BiliAPI.class);

    private final BiliAPI.API api;

    public BiliAPI() {
        OkHttpClient okHttpClient;
        Retrofit retrofit;

        okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request()
                            .newBuilder()
                            .build();
                    return chain.proceed(request);
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.bilibili.com")
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        this.api = retrofit.create(BiliAPI.API.class);
    }

    public String spi() {
        try {
            Response<String> response = api.spi().execute();

            return response.body();
        } catch (IOException e) {
            logger.error("API [https://api.bilibili.com/x/frontend/finger/spi] 请求错误", e);
        }

        return "";
    }

    private interface API {
        @GET("/x/frontend/finger/spi")
        Call<String> spi();
    }
}
