package net.dengzixu.blivedanmaku.api.bilibili.live;

import net.dengzixu.blivedanmaku.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class BiliAPI {
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(BiliAPI.class);

    public String spi() {
        try {
            URI uri = new URI("https://api.bilibili.com/x/frontend/finger/spi");

            return new HttpUtil().sendRequest(uri).body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("API [https://api.bilibili.com/x/frontend/finger/spi] 请求错误", e);
            return "";
        }
    }

}
