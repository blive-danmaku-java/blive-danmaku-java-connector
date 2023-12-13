package net.dengzixu.blivedanmaku.webscoket.client;


import net.dengzixu.blivedanmaku.Packet;
import net.dengzixu.blivedanmaku.PacketBuilder;
import net.dengzixu.blivedanmaku.enums.Operation;
import net.dengzixu.blivedanmaku.enums.ProtocolVersion;
import net.dengzixu.blivedanmaku.profile.BLiveServerProfile;
import net.dengzixu.blivedanmaku.profile.BLiveWebsocketClientProfile;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultBLiveWebsocketClient extends BLiveWebsocketClient {
    private static final Logger logger = LoggerFactory.getLogger(DefaultBLiveWebsocketClient.class);

    // 房间号
    private final Long roomID;
    // 弹幕服务器配置
    private final BLiveServerProfile bLiveServerProfile;
    // Websocket 客户端配置
    private final BLiveWebsocketClientProfile bLiveWebsocketClientProfile;
    // Websocket Listener
    private final WebSocketListener webSocketListener;

    // OkHttp Client
    private final OkHttpClient okHttpClient;
    // OkHttp Request
    private final Request request;
    // OkHttp Websocket
    private WebSocket webSocket;
    // Heartbeat Timer
    private Timer heartbeatTimer;

    // 主动断开连接标记
    private boolean activeDisconnect = false;
    // 重连计数器
    private final AtomicInteger reconnectCounter = new AtomicInteger(0);

    private Long lastConnectTime = -1L;

    /**
     * 默认 Websocket 客户端
     *
     * @param roomID                      房间号
     * @param bLiveServerProfile          弹幕服务器配置
     * @param bLiveWebsocketClientProfile Websocket 客户端配置
     */
    public DefaultBLiveWebsocketClient(Long roomID,
                                       BLiveServerProfile bLiveServerProfile,
                                       BLiveWebsocketClientProfile bLiveWebsocketClientProfile,
                                       WebSocketListener webSocketListener) {
        this.roomID = roomID;
        this.bLiveServerProfile = bLiveServerProfile;
        this.bLiveWebsocketClientProfile = bLiveWebsocketClientProfile;
        this.webSocketListener = webSocketListener;

        if (this.bLiveWebsocketClientProfile.maxReconnectTryTimes() == -1) {
            logger.warn("无重连次数上限，可能会触发无限重连");
        }

        // 创建 OkHttp Client
        this.okHttpClient = new OkHttpClient();

        // 构建请求
        this.request = new Request.Builder()
                .url("wss://" + this.bLiveServerProfile.host() + ":" + this.bLiveServerProfile.port() + "/sub")
                .build();
    }

    @Override
    public void connect() {
        if (null == webSocket) {
            webSocket = this.okHttpClient.newWebSocket(this.request, this.webSocketListener);
            // 设置连接时间
            this.lastConnectTime = System.currentTimeMillis();
        }
    }

    @Override
    public void disconnect() {
        this.activeDisconnect = true;
        this.webSocket.close(1000, "");
    }

    @Override
    public void reconnect() {
        // 如果是 Client 主动断开链接的，就不尝试重连
        if (this.activeDisconnect) {
            return;
        }

        // 等待 5 秒
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 是否达到重连计数器重置时间
        if (bLiveWebsocketClientProfile.resetReconnectCounter() != -1
                && System.currentTimeMillis() - lastConnectTime > bLiveWebsocketClientProfile.resetReconnectCounter()) {
            this.reconnectCounter.set(0);
        }

        // 重连次数是否超过最大次数
        if (this.bLiveWebsocketClientProfile.maxReconnectTryTimes() != -1
                && reconnectCounter.incrementAndGet() > bLiveWebsocketClientProfile.maxReconnectTryTimes()) {
            logger.error("[直播间: {}] 超过重连最大次数({})，放弃重连", this.roomID, this.bLiveWebsocketClientProfile.maxReconnectTryTimes());
            return;
        }

        lastConnectTime = System.currentTimeMillis();

        logger.info("[直播间: {}] 重新建立链接……({})", this.roomID, reconnectCounter.get());

        // 重新连接
        this.webSocket = okHttpClient.newWebSocket(this.request, this.webSocketListener);
    }

    @Override
    public void startHeartbeat() {
        if (null == heartbeatTimer) {
            heartbeatTimer = new Timer();
        }

        heartbeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Packet packet = PacketBuilder.newBuilder()
                        .protocolVersion(ProtocolVersion.HEARTBEAT)
                        .operation(Operation.HEARTBEAT)
                        .build();

                webSocket.send(new ByteString(packet.getBytes()));
            }
        }, 0, 1000 * 30);
    }

    @Override
    public void stopHeartbeat() {
        if (null != heartbeatTimer) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
    }
}
