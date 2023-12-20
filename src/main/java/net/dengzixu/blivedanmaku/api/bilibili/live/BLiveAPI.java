package net.dengzixu.blivedanmaku.api.bilibili.live;

import net.dengzixu.blivedanmaku.utils.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class BLiveAPI {
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(BLiveAPI.class);

    public String roomInit(Long roomID) {
        try {
            URI uri = new URI("https://api.live.bilibili.com/room/v1/Room/room_init?id=" + roomID);

            return new HttpUtil().sendRequest(uri).body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("API [https://api.live.bilibili.com/room/v1/Room/room_init] 请求错误", e);
            return "";
        }
    }

    public String roomInit(long roomID) {
        return this.roomInit(Long.valueOf(roomID));
    }

    public String getConf(Long roomID) {

        try {
            URI uri = new URI("https://api.live.bilibili.com/room/v1/Danmu/getConf?room_id=" + roomID);

            return new HttpUtil().sendRequest(uri).body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("API [https://api.live.bilibili.com/room/v1/Danmu/getConf] 请求错误", e);
            return "";
        }
    }

    public String getConf(long roomID) {
        return this.getConf(Long.valueOf(roomID));
    }

    public String getDanmuInfo(Long id, String sessData) {
        String cookie = StringUtils.isNotBlank(sessData) ? "SESSDATA=" + sessData : "";

        try {
            URI uri = new URI("https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo?id=" + id + "&type=0");

            return new HttpUtil().sendRequest(uri, cookie).body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("API [https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInf] 请求错误", e);
            return "";
        }
    }
}
