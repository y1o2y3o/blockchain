package com.rsp.controller;

import com.csp.web.Result;
import com.rsp.service.KeyCenterService;
import com.rsp.status.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zksfromusa
 */
@RestController
@RequestMapping("/heartBeat")
@Slf4j
public class HeartBeatController {
    @Resource
    private KeyCenterService keyCenterService;

    @Resource
    private Status status;

    // 心跳返回密钥
    @GetMapping("")
    public Result<?> handleEditRequest(@RequestParam("url") String url) {
        log.info(url);
        if(keyCenterService.addNewHost(url)){ // 存在若新的节点，则注册并分发密钥
            keyCenterService.updateSecretKeyStatus();
            keyCenterService.distributeKeyStatus();
        }
        return Result.success(status);
    }

}