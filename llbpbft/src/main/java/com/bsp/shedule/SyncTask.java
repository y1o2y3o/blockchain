package com.bsp.shedule;


import com.bsp.conf.ServerConfig;
import com.bsp.entity.Block;
import com.bsp.service.BlockService;
import com.csp.constant.Cons;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Objects;


/**
 * StaticScheduleTask
 *
 * @author 3
 */
@Component
@Slf4j
public class SyncTask {

    @Resource
    private ServerConfig serverConfig;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private BlockService blockService;

    /**
     * 定时向注册中心心跳链接
     */
    @Scheduled(fixedRate = Cons.COMMON_EXPIRED)
    @Async
    void heartConn() {
        String regUrl = serverConfig.getRegUrl();
        Objects.requireNonNull(restTemplate.getForObject(regUrl + "/heartBeat?url=" + serverConfig.getUrl(), Object.class));
        log.info("heartConn Task: 定时启动!");
    }

    /**
     * 测试程序
     */
//    @Scheduled(fixedRate = Cons.COMMON_EXPIRED)
//    @Async
    void test() throws Exception {
        blockService.save(Block.builder().build());
        log.info("test Task:定时启动!");
    }
}
