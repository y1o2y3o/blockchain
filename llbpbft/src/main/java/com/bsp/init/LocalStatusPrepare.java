package com.bsp.init;

import com.bsp.service.BlockService;
import com.bsp.status.LocalStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 从数据库加载本地状态
 */
@Component
@Slf4j
@Order(1)
public class LocalStatusPrepare implements CommandLineRunner {
    @Resource
    private BlockService blockService;

    @Resource
    private LocalStatus localStatus;

    @Override
    public void run(String... args) throws Exception {
        blockService.pullLocalStatus(); // 从数据库加载本地状态
        log.info("初始化本地状态:" + localStatus.toString());
    }
}
