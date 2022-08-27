package com.bsp.service.impl;

import com.alibaba.fastjson.JSON;
import com.bsp.service.MsgService;
import com.csp.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * <p>
 * Status判断服务
 * </p>
 *
 * @author zks
 * @since 2021-05-22
 */
@Service
@Slf4j
public class MsgServiceImpl implements MsgService {
    @Autowired
    private RestTemplate restTemplate;

    /**
     * post 消息
     *
     * @param url
     * @param data
     */
    @Override
    public void post(String url, Object data) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        // 访问下一个节点
        Objects.requireNonNull(restTemplate.postForObject(url,
                new HttpEntity<>(JSON.toJSONString(data), headers), String.class));
    }

    /**
     * get 消息
     * @param fullUrl
     * @return
     */
    @Override
    public Result<?> get(String fullUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        // 访问下一个节点
        return Objects.requireNonNull(restTemplate.getForObject(fullUrl, Result.class));
    }
}
