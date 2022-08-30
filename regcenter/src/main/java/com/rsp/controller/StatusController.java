package com.rsp.controller;

import com.alibaba.fastjson.JSON;
import com.csp.web.Result;
import com.rsp.service.KeyCenterService;
import com.rsp.status.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author zksfromusa
 */
@RestController
@RequestMapping("/status")
@Slf4j
public class StatusController {
    @Resource
    private KeyCenterService keyCenterService;

    @Resource
    private Status status;

    @Resource
    private RestTemplate restTemplate;

    // confirmHighBlock
    @GetMapping("/confirmHighBlock")
    public Result<?> confirmHighBlock() {
        // 广播confirmHighBlock消息
        status.getHostList().forEach(hostUrl -> {
            try{
                // 访问下一个节点
                Objects.requireNonNull(restTemplate.getForObject(hostUrl + "/status/confirmHighBlock", Result.class));
            } catch (Exception e){
                log.error(e.toString());
            }
        });
        return Result.success();
    }

    // confirmHighBlock2
    @GetMapping("/confirmHighBlock2")
    public Result<?> confirmHighBlock2() {
        // 广播confirmHighBlock消息
        status.getHostList().forEach(hostUrl -> {
            try{
                // 访问下一个节点
                Objects.requireNonNull(restTemplate.getForObject(hostUrl + "/status/confirmHighBlock2", Result.class));
            } catch (Exception e){
                log.error(e.toString());
            }
        });
        return Result.success();
    }

    // getStatus
    @GetMapping("/getStatus")
    public Status getStatus() {

        return status;
    }

    // incrViewNum
    @GetMapping("/incrViewNum")
    public Result<?> incrViewNum() {
        // 广播confirmHighBlock消息
        status.getHostList().forEach(hostUrl -> {
            // 访问下一个节点
            try{
                Objects.requireNonNull(restTemplate.getForObject(hostUrl + "/status/incrViewNum", Result.class));
            } catch (Exception e){
                log.error(e.toString());
            }
        });
        return Result.success();
    }
}