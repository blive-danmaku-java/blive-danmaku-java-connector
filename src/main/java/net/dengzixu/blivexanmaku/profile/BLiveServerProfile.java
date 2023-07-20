package net.dengzixu.blivexanmaku.profile;

/**
 * WebSocket 服务器配置
 *
 * @param host 主机
 * @param port 地址
 */
public record BLiveServerProfile(String host, String port) {
    public static BLiveServerProfile getDefault() {
        return new BLiveServerProfile("broadcastlv.chat.bilibili.com", "443");
    }
}
