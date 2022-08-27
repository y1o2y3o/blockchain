package dao;

import com.bsp.MainApplication;
import com.bsp.conf.ServerConfig;
import com.bsp.entity.Block;
import com.bsp.service.BlockService;
import com.bsp.utils.SnowFlakeIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Objects;

@SpringBootTest(classes = {MainApplication.class})
@RunWith(SpringRunner.class)
@Slf4j
public class TestDao {
    @Resource
    private BlockService blockService;
    @Resource
    private ServerConfig serverConfig;

    @Resource
    private RestTemplate restTemplate;

    @Test
    public void test01() {
        blockService.save(Block.builder().blockId(SnowFlakeIdUtil.getSnowflakeId()).build());
    }
}
