package net.dengzixu.blivexanmaku.profile;

/**
 * WebSocket 配置
 *
 * @param maxReconnectTryTimes  重连最大次数
 *                              <p> 0 为不重连</p>
 *                              <p> -1 为无限次重连</p>
 * @param resetReconnectCounter 重连计数器重置时间；
 *                              多久后重置重连计数器，单位毫秒，-1为不重置；
 *                              <b>建议根据重连次数合理设置时间，避免无限重连</b>
 */
public record BLiveWebsocketClientProfile(Integer maxReconnectTryTimes,
                                          Long resetReconnectCounter) {

    public static BLiveWebsocketClientProfile getDefault() {
        return new BLiveWebsocketClientProfile(10,
                60_000L);
    }
}
