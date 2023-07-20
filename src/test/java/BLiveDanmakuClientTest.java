import net.dengzixu.blivexanmaku.BLiveDanmakuClient;
import net.dengzixu.blivexanmaku.Packet;
import net.dengzixu.blivexanmaku.PacketResolver;
import net.dengzixu.blivexanmaku.enums.Operation;
import net.dengzixu.blivexanmaku.handler.Handler;

import java.util.List;

public class BLiveDanmakuClientTest {

    private static final long ROOM_ID = 77274;

    public static void main(String[] args) {
        BLiveDanmakuClient bLiveDanmakuClient = BLiveDanmakuClient.getInstance(ROOM_ID);

        bLiveDanmakuClient.addHandler(new Handler() {
            @Override
            public void doHandler(byte[] bytes) {
                PacketResolver packetResolver = new PacketResolver(bytes);

                List<Packet> packets = packetResolver.resolve();

                packets.forEach(packet -> {
                    if (packet.operation().equals(Operation.MESSAGE)) {
                        System.out.println(new String(packet.body()));
                    }
                });

            }
        });

        bLiveDanmakuClient.connect();
    }
}
