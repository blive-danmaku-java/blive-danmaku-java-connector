import net.dengzixu.blivedanmaku.Packet;
import net.dengzixu.blivedanmaku.PacketResolver;
import net.dengzixu.blivedanmaku.BLiveDanmakuClient;
import net.dengzixu.blivedanmaku.profile.BLiveAuthProfile;

import java.util.List;

public class BLiveDanmakuClientTest {

    private static final long ROOM_ID = 13308358;

    public static void main(String[] args) {
        final BLiveAuthProfile bLiveAuthProfile = new BLiveAuthProfile(4283693, "1d97b4bd,1709224198,1efdd*91");

        BLiveDanmakuClient bLiveDanmakuClient = BLiveDanmakuClient.getInstance(ROOM_ID, bLiveAuthProfile);

        bLiveDanmakuClient.addHandler(bytes -> {
            PacketResolver packetResolver = new PacketResolver(bytes);

            List<Packet> packets = packetResolver.resolve();

            packets.forEach(System.out::println);

        });

        bLiveDanmakuClient.connect();
    }
}
