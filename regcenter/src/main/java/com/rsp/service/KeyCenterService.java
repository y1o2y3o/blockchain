package com.rsp.service;

import org.springframework.stereotype.Service;

/**
 * 密钥service
 */
public interface KeyCenterService {
    boolean addNewHost(String url);

    void updateSecretKeyStatus();

    void distributeKeyStatus();
}
