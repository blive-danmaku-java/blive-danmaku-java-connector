package net.dengzixu.blivexanmaku;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dengzixu.blivexanmaku.api.bilibili.live.BLiveAPI;
import net.dengzixu.blivexanmaku.body.AuthBody;
import net.dengzixu.blivexanmaku.enums.Operation;
import net.dengzixu.blivexanmaku.enums.ProtocolVersion;
import net.dengzixu.blivexanmaku.filter.Filter;
import net.dengzixu.blivexanmaku.handler.Handler;
import net.dengzixu.blivexanmaku.profile.BLiveServerProfile;
import net.dengzixu.blivexanmaku.profile.BLiveWebsocketClientProfile;
import net.dengzixu.blivexanmaku.webscoket.client.BLiveWebsocketClient;
import net.dengzixu.blivexanmaku.webscoket.client.DefaultBLiveWebsocketClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
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
                               BLiveServerProfile bLiveServerProfile,
                               BLiveWebsocketClientProfile bLiveWebsocketClientProfile) {
        this.roomID = roomID;
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
                                                 BLiveServerProfile bLiveServerProfile,
                                                 BLiveWebsocketClientProfile bLiveWebsocketClientProfile) {
        if (null == instanceMap.get(roomID)) {
            synchronized (BLiveDanmakuClient.class) {
                if (null == instanceMap.get(roomID)) {
                    instanceMap.put(roomID, new BLiveDanmakuClient(roomID,
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
                                                 BLiveServerProfile bLiveServerProfile,
                                                 BLiveWebsocketClientProfile bLiveWebsocketClientProfile) {
        return getInstance(Long.valueOf(roomID),
                bLiveServerProfile,
                bLiveWebsocketClientProfile);
    }

    /**
     * 获取弹幕客户端实例
     *
     * @param roomID 房间号
     * @return 弹幕客户端实例
     */
    public static BLiveDanmakuClient getInstance(long roomID) {
        return getInstance(roomID,
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
                // TODO Auth
                logger.info("[直播间: {}] 准备建立链接", roomID);

                this.auth(webSocket);

                super.onOpen(webSocket, response);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                handlers.forEach(handler -> executor.execute(() -> {
                    handler.doHandler(bytes.toByteArray());
                }));
                super.onMessage(webSocket, bytes);
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

                super.onFailure(webSocket, t, response);
            }

            public void auth(WebSocket webSocket) {
                logger.info("[直播间: {}] 进行身份认证……", roomID);

                BLiveAPI bLiveAPI = new BLiveAPI();

                String token = "";
                long anchorUID = 0L;

                // 获取 Token 与主播 UID
                try {
                    token = new ObjectMapper()
                            .readValue(bLiveAPI.getConf(roomID), JsonNode.class)
                            .get("data").get("token").asText();

                    anchorUID = new ObjectMapper()
                            .readValue(bLiveAPI.roomInit(roomID), JsonNode.class)
                            .get("data").get("uid").asLong();

                    logger.info("[直播间: {}] 主播 UID: {}", roomID, anchorUID);
                } catch (JsonProcessingException e) {
                    logger.error("JSON 转换失败", e);
                    throw new RuntimeException(e);
                }
                // 构建 Packet
                Packet packet = PacketBuilder.newBuilder()
                        .protocolVersion(ProtocolVersion.NORMAL)
                        .operation(Operation.USER_AUTHENTICATION)
                        .body(new AuthBody(anchorUID, roomID, token).toJsonBytes())
                        .build();

                // 发送 Packet
                webSocket.send(new ByteString(packet.getBytes()));

                // 发送心跳包
                websocketClient.startHeartbeat();

            }


        };
    }
}
