package com.bsp.shedule;


import com.bsp.conf.constant.Cons;
import com.bsp.utils.LoggerHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * StaticScheduleTask
 *
 * @author 3
 */
@Component
@Slf4j
public class SyncTask {

    /**
     * 定时向注册中心心跳链接
     */
    @Scheduled(fixedRate = Cons.APPCASH_CHECK_EXPIRED)
    @Async
    void scanAppCash() {
        log.info("scanAppCash: 定时任务启动!");

    }

    /**
     * 定时清理悬空Attachment
     */
//    @Scheduled(fixedRate = Global.CHECK_SUSPEND_ATTACHMENT)
    @Async
    void scanAttachment() throws Exception {

    }
}
