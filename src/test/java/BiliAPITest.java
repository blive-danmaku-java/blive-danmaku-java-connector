import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dengzixu.blivedanmaku.api.bilibili.live.BiliAPI;
import org.junit.jupiter.api.Test;

public class BiliAPITest {
    private static final BiliAPI bLiveAPI = new BiliAPI();

    @Test
    void testSpi() {
        String apiResponse = bLiveAPI.spi();

        System.out.println(apiResponse);
        try {
            JsonNode jsonNode = new ObjectMapper().readValue(apiResponse, JsonNode.class);

            System.out.println("buvid3: " + jsonNode.get("data").get("b_3").asText());
            System.out.println("buvid4: " + jsonNode.get("data").get("b_4").asText());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
