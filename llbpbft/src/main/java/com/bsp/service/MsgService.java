package com.bsp.service;

import com.csp.web.Result;

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

    /**
     * get 消息
     *
     * @param fullUrl
     */
    Result<?> get(String fullUrl);
}
