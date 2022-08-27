package com.bsp.status;

import com.csp.sig.SecretKeys;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zksfromusa
 */
@Component
@Data
public class GlobalStatus {
    private List<String> hostList = new ArrayList<>(); // 节点ip:port列表
    private SecretKeys secretKeys = new SecretKeys(); // 本节点的私钥
    private Map<String, SecretKeys> hostSecretKeyMap = new HashMap<>(); // 所有节点的公钥
}
