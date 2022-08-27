package com.rsp.status;

import com.csp.sig.SecretKeys;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author zksfromusa
 */
@Component
@Data
public class Status {
    private String g;
    private String sk; // 系统主私钥
    private String pk; // 系统主公钥
    // 门限(t, n)
    private int n;
    private int t;

    private List<String> hostList = new ArrayList<>(); // 节点ip:port列表
    private Map<String, String> hostSecretKeyMap = new HashMap<>(); // 所有节点的密钥信息
    private Map<String, String> hostPubKeyMap = new HashMap<>(); // 所有节点的公钥钥信息
}
