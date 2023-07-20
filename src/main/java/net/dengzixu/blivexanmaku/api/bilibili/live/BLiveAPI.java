package net.dengzixu.blivexanmaku.api.bilibili.live;

import net.dengzixu.blivexanmaku.constant.Constant;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.io.IOException;

public class BLiveAPI {
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(BLiveAPI.class);

    private final API api;


    public BLiveAPI() {
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
                .baseUrl(Constant.BILIBILI_API_URL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        this.api = retrofit.create(API.class);
    }

    public String roomInit(Long roomID) {
        try {
            Response<String> response = api.roomInit(roomID).execute();

            return response.body();
        } catch (IOException e) {
            logger.error("API 请求错误", e);
            return null;
        }
    }

    public String roomInit(long roomID) {
        return this.roomInit(Long.valueOf(roomID));
    }

    public String getConf(Long roomID) {
        try {
            Response<String> response = api.getConf(roomID).execute();

            return response.body();
        } catch (IOException e) {
            logger.error("API 请求错误", e);
            return null;
        }
    }

    public String getConf(long roomID) {
        return this.getConf(Long.valueOf(roomID));
    }


    private interface API {
        @GET("/room/v1/Room/room_init")
        @Headers({"Content-Type: application/json; charset=UTF-8"})
        Call<String> roomInit(@Query("id") Long roomID);

        @GET("/room/v1/Danmu/getConf")
        @Headers({"Content-Type: application/json; charset=UTF-8"})
        Call<String> getConf(@Query("room_id") Long roomID);
    }
}