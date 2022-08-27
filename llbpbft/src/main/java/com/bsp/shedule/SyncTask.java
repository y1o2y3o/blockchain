package com.bsp.shedule;


import com.alibaba.fastjson.JSON;
import com.bsp.conf.ServerConfig;
import com.csp.constant.Cons;
import com.csp.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
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

    /**
     * 定时向注册中心心跳链接
     */
    @Scheduled(fixedRate = Cons.COMMON_EXPIRED)
    @Async
    void heartConn() {
        String regUrl = serverConfig.getRegUrl();
        log.info(Objects.requireNonNull(restTemplate.getForObject(regUrl + "/heartBeat?url=" + serverConfig.getUrl(),
                Object.class)).toString());
        log.info("heartConn Task: 定时启动!");
    }

    /**
     * 定时清理悬空Attachment
     */
//    @Scheduled(fixedRate = Global.CHECK_SUSPEND_ATTACHMENT)
    @Async
    void scanAttachment() throws Exception {

    }
}
