package com.bsp.service;

import com.csp.web.Result;

import java.util.List;

/**
 * <p>
 * MsgService
 * </p>
 *
 * @author zks
 * @since 2021-05-22
 */
public interface MsgService {
    /**
     * post 消息
     *
     * @param url
     * @param data
     */
    void post(String url, Object data);

    /**
     * post 消息
     *
     * @param url
     * @param data
     */
    boolean testAndPost(String url, Object data);

    /**
     * testAndGet
     *
     */
    boolean testAndGet(String fullUrl);

    /**
     * get 消息
     *
     * @param fullUrl
     */
    Result<?> get(String fullUrl);

    /**
     * 将当前最高区块发送给领导
     *
     */
    void confirmHighBlock();

    /**
     * 将当前最高区块发送给所有节点
     *
     */
    void confirmHighBlock2();

    /**
     * 广播post
     *
     */
    void broadcastPost(List<String> urlList, Object data);

    /**
     * 广播get
     *
     */
    void broadcastGet(List<String> urlList);

    /**
     * syncBlockHeight
     *
     */
    void syncBlockHeight();
}
