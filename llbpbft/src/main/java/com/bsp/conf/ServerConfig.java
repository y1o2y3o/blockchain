package com.bsp.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 当前服务器配置
 *
 * @author zksfromusa
 */
@Configuration
public class ServerConfig {

    @Value("${server.port}")
    private Integer port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${regcenter.ip}")
    private String regIp;

    /**
     * 项目获取当前服务的IP端口地址
     *
     * @return
     */
    public String getUrl() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert address != null;
        return "http://" + address.getHostAddress() + ":" + port + "/" + contextPath;
    }

}
