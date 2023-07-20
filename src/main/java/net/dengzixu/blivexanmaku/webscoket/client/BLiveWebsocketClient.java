package net.dengzixu.blivexanmaku.webscoket.client;

public abstract class BLiveWebsocketClient {
    /**
     * 建立 Websocket 链接
     */
    public abstract void connect();

    /**
     * 断开 Websocket 链接
     */
    public abstract void disconnect();

    /**
     * 意外断开时，重新建立 Websocket 链接
     */
    public abstract void reconnect();

    /**
     * 开始心跳
     */
    public abstract void startHeartbeat();

    /**
     * 停止心跳
     */
    public abstract void stopHeartbeat();
}
