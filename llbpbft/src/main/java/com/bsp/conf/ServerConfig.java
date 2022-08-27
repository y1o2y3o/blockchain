package com.bsp.conf;

import lombok.Data;
import lombok.Getter;
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
@Getter
public class ServerConfig {

    @Value("${server.port}")
    private Integer port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${regcenter.host}")
    private String regHost;

    @Value("${regcenter.port}")
    private String regPort;

    @Value("${regcenter.contextPath}")
    private String regContextPath;

    /**
     * 项目获取当前服务的IP端口地址
     *
     * @return String
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

    /**
     * 获取服务注册中心的IP端口地址
     *
     * @return String
     */
    public String getRegUrl() {
        return "http://" + getRegHost() + ":" + getRegPort() + getRegContextPath();
    }

}
