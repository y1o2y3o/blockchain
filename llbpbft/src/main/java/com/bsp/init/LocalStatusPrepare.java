package com.bsp.init;

import com.bsp.service.BlockService;
import com.bsp.status.LocalStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class LocalStatusPrepare implements CommandLineRunner {
    @Resource
    private BlockService blockService;

    @Resource
    private LocalStatus localStatus;

    @Override
    public void run(String... args) throws Exception {
        blockService.pullLocalStatus();
        log.info("初始化本地状态:" + localStatus.toString());
    }
}
