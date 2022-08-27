package com.bsp.service.impl;

import com.bsp.conf.ServerConfig;
import com.bsp.service.StatusService;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.csp.util.ParamComputing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * StatusService
 * </p>
 *
 * @author zks
 * @since 2021-05-22
 */
@Service
public class StatusServiceImpl implements StatusService {

    @Autowired
    private LocalStatus localStatus;

    @Autowired
    private GlobalStatus globalStatus;

    @Autowired
    private ServerConfig serverConfig;

    @Override
    public String leader(Integer viewNumber) {
        List<String> hostList = globalStatus.getHostList();
        int n = hostList.size();
        return hostList.get(viewNumber % n);
    }

    @Override
    public Boolean isCurrentLeader() {
        String curLeader = leader(localStatus.getCurViewNumber());
        return Objects.equals(curLeader, serverConfig.getUrl());
    }

    @Override
    public Integer curMaxFaultToleranceNum() {
        int n = curHostsNum();
        return ParamComputing.getF(n);
    }

    @Override
    public Integer curHostsNum() {
        return globalStatus.getHostList().size();
    }

    @Override
    public Boolean isCurrentReplica() {
        return !isCurrentLeader();
    }

    @Override
    public Boolean isLeader(String url, Integer viewNumber) {
        return leader(viewNumber) != null && leader(viewNumber).equals(url);
    }
}
