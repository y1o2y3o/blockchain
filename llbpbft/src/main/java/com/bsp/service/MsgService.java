package com.bsp.service;

/**
 * <p>
 * Status判断服务
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
}
