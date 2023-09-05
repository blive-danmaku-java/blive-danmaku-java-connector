import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dengzixu.blivexanmaku.api.bilibili.live.BLiveAPI;
import org.junit.jupiter.api.Test;

public class BLiveAPITest {
    private static final BLiveAPI bLiveAPI = new BLiveAPI();

    @Test
    public void testRoomInit() {
        String apiResponse = bLiveAPI.roomInit(77274);

        try {
            JsonNode jsonNode = new ObjectMapper().readValue(apiResponse, JsonNode.class);

            System.out.println(jsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetConf() {
        String apiResponse = bLiveAPI.getConf(77274);

        try {
            JsonNode jsonNode = new ObjectMapper().readValue(apiResponse, JsonNode.class);

            System.out.println(jsonNode);
            System.out.println("Token: " + jsonNode.get("data").get("token").asText());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetDanmuInfo() {
        String apiResponse = bLiveAPI.getDanmuInfo(77274L, "a");

        try {
            JsonNode jsonNode = new ObjectMapper().readValue(apiResponse, JsonNode.class);

            System.out.println(jsonNode);
            System.out.println("Token: " + jsonNode.get("data").get("token").asText());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
