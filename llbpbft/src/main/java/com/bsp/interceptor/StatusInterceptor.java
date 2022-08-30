package com.bsp.interceptor;

import com.bsp.conf.ServerConfig;
import com.bsp.service.BlockService;
import com.bsp.service.MsgService;
import com.bsp.service.StatusService;
import com.bsp.signatures.ThresholdSignature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 拦截器
 *
 * @author 3
 */
public class StatusInterceptor implements HandlerInterceptor {
    @Autowired
    private LocalStatus localStatus;
    @Autowired
    private GlobalStatus globalStatus;
    @Autowired
    private ThresholdSignature thresholdSignature;

    @Autowired
    private BlockService blockService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private MsgService msgService;

    @Autowired
    private ServerConfig serverConfig;


    // 之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //statusService.getAndUpdateGlobalStatus();
        return true;
    }
}
