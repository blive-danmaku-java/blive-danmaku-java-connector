package net.dengzixu.blivedanmaku;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dengzixu.blivedanmaku.api.bilibili.live.BLiveAPI;
import net.dengzixu.blivedanmaku.api.bilibili.live.BiliAPI;
import net.dengzixu.blivedanmaku.body.AuthBody;
import net.dengzixu.blivedanmaku.enums.Operation;
import net.dengzixu.blivedanmaku.enums.ProtocolVersion;
import net.dengzixu.blivedanmaku.filter.Filter;
import net.dengzixu.blivedanmaku.handler.Handler;
import net.dengzixu.blivedanmaku.profile.BLiveAuthProfile;
import net.dengzixu.blivedanmaku.profile.BLiveServerProfile;
import net.dengzixu.blivedanmaku.profile.BLiveWebsocketClientProfile;
import net.dengzixu.blivedanmaku.webscoket.client.BLiveWebsocketClient;
import net.dengzixu.blivedanmaku.webscoket.client.DefaultBLiveWebsocketClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BLiveDanmakuClient {
    private static final Logger logger = LoggerFactory.getLogger(BLiveDanmakuClient.class);

    // 弹幕客户端实例
    private static final Map<Long, BLiveDanmakuClient> instanceMap = new ConcurrentHashMap<>();

    // 房间号
    private final Long roomID;
    // 认证配置
    private final BLiveAuthProfile bLiveAuthProfile;
    // 弹幕服务器配置
    private final BLiveServerProfile bLiveServerProfile;
    // Websocket 客户端配置
    private final BLiveWebsocketClientProfile bLiveWebsocketClientProfile;

    // WebsocketClient
    private final BLiveWebsocketClient websocketClient;

    // Handler 列表
    private final CopyOnWriteArrayList<Handler> handlers = new CopyOnWriteArrayList<>();
    // Filter 列表
    private final CopyOnWriteArrayList<Filter> filters = new CopyOnWriteArrayList<>();

    // 线程池
    private static final ExecutorService executor = Executors.newCachedThreadPool();


    /**
     * 创建弹幕客户端
     *
     * @param roomID                      房间号
     * @param bLiveServerProfile          弹幕服务器配置
     * @param bLiveWebsocketClientProfile Websocket 客户端配置
     */
    private BLiveDanmakuClient(Long roomID,
                               BLiveAuthProfile bLiveAuthProfile,
                               BLiveServerProfile bLiveServerProfile,
                               BLiveWebsocketClientProfile bLiveWebsocketClientProfile) {
        this.roomID = roomID;
        this.bLiveAuthProfile = bLiveAuthProfile;
        this.bLiveServerProfile = bLiveServerProfile;
        this.bLiveWebsocketClientProfile = bLiveWebsocketClientProfile;

        this.websocketClient = new DefaultBLiveWebsocketClient(this.roomID,
                this.bLiveServerProfile,
                this.bLiveWebsocketClientProfile,
                this.createWebSocketListener());
    }

    /**
     * 获取弹幕客户端实例
     *
     * @param roomID                      房间号
     * @param bLiveServerProfile          弹幕服务器配置
     * @param bLiveWebsocketClientProfile Websocket 客户端配置
     * @return 弹幕客户端实例
     */
    public static BLiveDanmakuClient getInstance(Long roomID,
                                                 BLiveAuthProfile bLiveAuthProfile,
                                                 BLiveServerProfile bLiveServerProfile,
                                                 BLiveWebsocketClientProfile bLiveWebsocketClientProfile) {
        if (null == instanceMap.get(roomID)) {
            synchronized (BLiveDanmakuClient.class) {
                if (null == instanceMap.get(roomID)) {
                    instanceMap.put(roomID, new BLiveDanmakuClient(roomID,
                            bLiveAuthProfile,
                            bLiveServerProfile,
                            bLiveWebsocketClientProfile));
                }
            }
        }
        return instanceMap.get(roomID);
    }

    /**
     * 获取弹幕客户端实例
     *
     * @param roomID                      房间号
     * @param bLiveServerProfile          弹幕服务器配置
     * @param bLiveWebsocketClientProfile Websocket 客户端配置
     * @return 弹幕客户端实例
     */
    public static BLiveDanmakuClient getInstance(long roomID,
                                                 BLiveAuthProfile bLiveAuthProfile,
                                                 BLiveServerProfile bLiveServerProfile,
                                                 BLiveWebsocketClientProfile bLiveWebsocketClientProfile) {
        return getInstance(Long.valueOf(roomID),
                bLiveAuthProfile,
                bLiveServerProfile,
                bLiveWebsocketClientProfile);
    }

    /**
     * @param roomID           房间号
     * @param bLiveAuthProfile 认证配置
     * @return 弹幕客户端实例
     */
    public static BLiveDanmakuClient getInstance(long roomID, BLiveAuthProfile bLiveAuthProfile) {
        return getInstance(roomID,
                bLiveAuthProfile,
                BLiveServerProfile.getDefault(),
                BLiveWebsocketClientProfile.getDefault());
    }

    /**
     * 获取弹幕客户端实例
     *
     * @param roomID 房间号
     * @return 弹幕客户端实例
     */
    public static BLiveDanmakuClient getInstance(long roomID) {
        return getInstance(roomID,
                BLiveAuthProfile.getAnonymous(),
                BLiveServerProfile.getDefault(),
                BLiveWebsocketClientProfile.getDefault());
    }

    /**
     * 添加 Handler
     * <b>Handler 没有先后顺序</b>
     *
     * @param handler Handler 对象
     * @return BLiveDanmakuClient Chain
     */
    public BLiveDanmakuClient addHandler(Handler handler) {
        handlers.add(handler);

        return this;
    }

    /**
     * 移除 Handler
     *
     * @param handler Handler 对象
     * @return BLiveDanmakuClient Chain
     */
    public BLiveDanmakuClient removeHandler(Handler handler) {
        handlers.remove(handler);

        return this;
    }

    /**
     * 添加 Filter
     * <b>Filter 没有先后顺序</b>
     *
     * @param filter Filter 对象
     * @return BLiveDanmakuClient Chain
     */
    public BLiveDanmakuClient addFilter(Filter filter) {
        filters.add(filter);
        return this;
    }

    /**
     * 移除 Filter
     *
     * @param filter Filter 对象
     * @return BLiveDanmakuClient Chain
     */
    public BLiveDanmakuClient removeFilter(Filter filter) {
        filters.remove(filter);
        return this;
    }

    /**
     * 链接弹幕服务器
     *
     * @return BLiveDanmakuClient Chain
     */
    public BLiveDanmakuClient connect() {
        this.websocketClient.connect();
        return this;
    }

    /**
     * 断开弹幕服务器
     *
     * @return BLiveDanmakuClient Chain
     */
    public BLiveDanmakuClient disconnect() {
        this.websocketClient.disconnect();

        return this;
    }

    private WebSocketListener createWebSocketListener() {
        return new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                logger.info("[直播间: {}] 准备建立链接", roomID);

                try {
                    this.auth(webSocket);
                } catch (Exception e) {
                    logger.error("[直播间: {}] 身份认证失败", roomID, e);
                }
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                handlers.forEach(handler -> executor.execute(() -> {
                    handler.doHandler(bytes.toByteArray());
                }));
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                logger.error("[直播间: {}] 连接意外断开", roomID, t);

                websocketClient.reconnect();
            }

            public void auth(WebSocket webSocket) throws JsonProcessingException {
                logger.info("[直播间: {}] 进行身份认证……", roomID);

                // BLiveAPI
                final BLiveAPI bLiveAPI = new BLiveAPI();
                // BiliAPI
                final BiliAPI biliAPI = new BiliAPI();

                // 获取 Token
                JsonNode danmuInfoJsonNode = new ObjectMapper()
                        .readValue(bLiveAPI.getDanmuInfo(roomID, bLiveAuthProfile.sessData()), JsonNode.class);
                String token = danmuInfoJsonNode.get("data").get("token").asText();

                // 获取认证用的 UID
                Long authUID = bLiveAuthProfile.uid();
                // 判断 UID 为 0 时，或 sessData 为空时，如果未设置，UID 取 0
                if (authUID.equals(0L) || StringUtils.isBlank(bLiveAuthProfile.sessData())) {
                    logger.warn("[直播间: {}] 未设置认证 UID，使用匿名模式。建议正确配置 UID 与 SESSDATA 避免出现连接中断等问题。", roomID);
                    authUID = 0L;
                } else if (!authUID.equals(0L) && StringUtils.isBlank(bLiveAuthProfile.sessData())) {
                    logger.warn("[直播间: {}] 已设置认证 UID，但未设置 SESSDATA，使用匿名模式。建议正确配置 UID 与 SESSDATA 避免出现连接中断等问题", roomID);

                    authUID = 0L;
                }
                logger.info("[直播间: {}] 使用认证 UID: {}", roomID, authUID);

                // 获取 buvid3
                JsonNode spiJsonNode = new ObjectMapper()
                        .readValue(biliAPI.spi(), JsonNode.class);

                String buvid = spiJsonNode.get("data").get("b_3").asText();
                logger.info("[直播间: {}] 获取到 buvid: {}", roomID, buvid);

                // 构建 Packet
                Packet packet = PacketBuilder.newBuilder()
                        .protocolVersion(ProtocolVersion.HEARTBEAT)
                        .operation(Operation.USER_AUTHENTICATION)
                        .body(new AuthBody(authUID, roomID, buvid, token).toJsonBytes())
                        .build();

                // 发送 Packet
                webSocket.send(new ByteString(packet.getBytes()));

                // 发送心跳包
                websocketClient.startHeartbeat();
            }
        };
    }
}
