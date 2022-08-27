package sig;

import com.bsp.MainApplication;
import com.bsp.conf.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Objects;

@SpringBootTest(classes = {MainApplication.class})
@RunWith(SpringRunner.class)
@Slf4j
public class TestHeartBeatCobb {
    @Resource
    private ServerConfig serverConfig;

    @Resource
    private RestTemplate restTemplate;

    @Test
    public void heartConn() {
        String regUrl = serverConfig.getRegUrl();

        log.info(Objects.requireNonNull(restTemplate.getForObject(regUrl + "/heartBeat?url=" + serverConfig.getUrl(),
                Object.class)).toString());
        log.info("heartConn Task: 定时启动!");

    }
}
