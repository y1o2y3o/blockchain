package com.bsp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bsp.entity.Block;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Status判断服务
 * </p>
 *
 * @author zks
 * @since 2021-05-22
 */
public interface StatusService {
    /**
     * 查询视图viewNumber对应的领导
     *
     * @param viewNumber
     * @return 领导节点url
     */
    String leader(Integer viewNumber);

    /**
     * 是否为当前视图的领导
     *
     * @return
     */
    Boolean isCurrentLeader();

    /**
     * 最大容错数
     *
     * @return
     */
    Integer curMaxFaultToleranceNum();

    /**
     * 当前最大主机节点数
     *
     * @return
     */
    Integer curHostsNum();

    /**
     * 是否为当前视图的副本
     *
     * @return
     */
    Boolean isCurrentReplica();

    /**
     * isLeader
     *
     * @param url
     * @param viewNumber
     * @return
     */
    Boolean isLeader(String url, Integer viewNumber);

}
