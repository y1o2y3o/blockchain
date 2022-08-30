package com.bsp.service.impl;

import com.alibaba.fastjson.JSON;
import com.bsp.conf.ServerConfig;
import com.bsp.entity.Block;
import com.bsp.enums.MessageEnum;
import com.bsp.service.BlockService;
import com.bsp.service.MsgService;
import com.bsp.service.StatusService;
import com.bsp.signatures.ThresholdSignature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.bsp.web.Message;
import com.csp.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Autowired
    private BlockService blockService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private GlobalStatus globalStatus;

    @Autowired
    private LocalStatus localStatus;

    @Autowired
    private ThresholdSignature signature;

    @Autowired
    private ServerConfig serverConfig;

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
        try {
            Objects.requireNonNull(restTemplate.postForObject(url,
                    new HttpEntity<>(JSON.toJSONString(data), headers), String.class));
        } catch (Exception e) {
            log.error(e.toString());
        }

    }

    @Override
    public boolean testAndPost(String url, Object data) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        // 访问下一个节点
        try {
            Objects.requireNonNull(restTemplate.postForObject(url,
                    new HttpEntity<>(JSON.toJSONString(data), headers), String.class));
            return true;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    @Override
    public boolean testAndGet(String fullUrl) {
        try {
            Objects.requireNonNull(restTemplate.getForObject(fullUrl, Result.class));
            return true;
        } catch (Exception e) {
            log.error(fullUrl);
            log.error(e.toString());
            return false;
        }
    }

    /**
     * get 消息
     *
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

    /**
     * 给当前领导发送当前最高区块
     */
    @Override
    public void confirmHighBlock() {
        blockService.pullLocalStatusWithoutView();
        Block highBlock = localStatus.getPreparedBlock(); // 当前最高区块
        highBlock.setFlag(null);
        String partialSig = signature.partialSign(highBlock); // 部分签名
        int viewNum = localStatus.getCurViewNumber(); // 当前视图
        // 构造消息
        Message msg = Message.builder()
                .type(MessageEnum.PROPOSAL_VOTE.toString()) // PROPOSAL_VOTE
                .partialSig(partialSig)
                .block(highBlock)
                .url(serverConfig.getUrl())
                .viewNumber(highBlock.getViewNumber()) // 上个视图
                .build();
        post(statusService.leader(viewNum) + "/message/PROPOSAL_VOTE", msg); // 给当前领导发送信息
    }

    @Override
    public void confirmHighBlock2() {
        blockService.pullLocalStatusWithoutView();
        Block highBlock = localStatus.getPreparedBlock(); // 当前最高区块
        highBlock.setFlag(null);
        String partialSig = signature.partialSign(highBlock); // 部分签名
        int viewNum = localStatus.getCurViewNumber(); // 当前视图
        // 构造消息
        Message msg = Message.builder()
                .type(MessageEnum.PROPOSAL_VOTE.toString()) // PROPOSAL_VOTE
                .partialSig(partialSig)
                .block(highBlock)
                .url(serverConfig.getUrl())
                .viewNumber(highBlock.getViewNumber()) // 上个视图
                .build();
        List<String> urlList = globalStatus.getHostList().stream().map(url -> url + "/pbft/message/COMMIT").collect(Collectors.toList());
        broadcastPost(urlList, msg); // 给当所有副本节点发送信息
    }

    /**
     * 广播post
     *
     * @param urlList
     * @param data
     */
    @Override
    public void broadcastPost(List<String> urlList, Object data) {
        urlList.forEach(url -> {
            try {
                post(url, data);
            } catch (Exception e) {
                log.error(e.toString());
            }
        });
    }

    @Override
    public void broadcastGet(List<String> urlList) {
        urlList.forEach(this::get);
    }

}
