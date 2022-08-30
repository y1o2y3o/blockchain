package com.rsp.service.impl;

import com.alibaba.fastjson.JSON;
import com.csp.util.ParamComputing;
import com.rsp.service.KeyCenterService;
import com.rsp.status.Status;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class KeyCenterServiceImpl implements KeyCenterService {

    @Resource
    private Status status;

    @Resource
    private RestTemplate restTemplate;

    // 门限签名参数
    // 固定写死
    private Pairing bp;
    private Field G1;
    private Field Zr;

    // 传递的参数
    private Element g;
    private Element SK; // 系统主私钥
    private Element PK; // 系统主公钥
    // 门限(t, n)
    private int n = 9;
    private int t = 4;

    // 多项式
    private Element[] coeff;

    @Override
    public synchronized boolean addNewHost(String url) {
        List<String> hostList = status.getHostList();
        if (hostList.contains(url)) {
            return false;
        }
        hostList.add(url);
        return true;
    }


    @Override
    public synchronized void updateSecretKeyStatus() {
        bp = PairingFactory.getPairing("regcenter/a.properties");
        G1 = bp.getG1();
        Zr = bp.getZr();

        g = G1.newRandomElement().duplicate();   // g
        SK = Zr.newRandomElement().duplicate(); // 系统主私钥
        PK = g.duplicate().powZn(SK.duplicate()).duplicate(); // 系统主公钥
        n = status.getHostList().size(); // 总结点数
        t = n - ParamComputing.getF(n); // 至少获得t个签名，门限
        // 生成多项式
        coeff = new Element[t];
        coeff[0] = SK.duplicate();
        for (int j = 1; j < t; j++) {
            coeff[j] = Zr.newRandomElement().duplicate();

        }

        // 计算分私钥xi[] 分公钥vi[]
        List<Element> X = new ArrayList<>();
        List<Element> V = new ArrayList<>();
        for (int i = 1; i <= n; ++i) {
            // 计算分私钥xi = P(i)
            Element xi = P(i).duplicate();
            X.add(xi.duplicate());
            // 计算分公钥vi
            Element vi = g.duplicate().powZn(xi.duplicate()).duplicate();
            V.add(vi);
        }

        // 更新status
        status.setG(Base64.encodeBase64String(g.duplicate().toBytes()));
        status.setPk(Base64.encodeBase64String(PK.duplicate().toBytes()));
        status.setSk(Base64.encodeBase64String(SK.duplicate().toBytes()));
        status.setN(n);
        status.setT(t);
        status.setHostSecretKeyMap(new HashMap<>());
        status.setHostPubKeyMap(new HashMap<>());
        for (int i = 0; i < n; ++i) {
            String hostUrl = status.getHostList().get(i);
            byte[] partialSK = X.get(i).duplicate().toBytes();
            byte[] partialPK = V.get(i).duplicate().toBytes();
            status.getHostSecretKeyMap().put(hostUrl, Base64.encodeBase64String(partialSK));
            status.getHostPubKeyMap().put(hostUrl, Base64.encodeBase64String(partialPK));
        }
    }

    @Override
    public synchronized void distributeKeyStatus() {
        // 广播密钥信息消息
        status.getHostList().forEach(hostUrl -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json;charset=UTF-8");
            try {
                // 访问下一个节点
                Objects.requireNonNull(restTemplate.postForObject(hostUrl + "/status/update",
                        new HttpEntity<>(JSON.toJSONString(status), headers), String.class));
            } catch (Exception e) {
                log.error(e.toString());
            }

        });
    }

    // Z_p上的t-1多项式P(i)
    private Element P(int i) {
        Element res = Zr.newZeroElement().duplicate();
        for (int j = 0; j < t; j++) {
            res = res.duplicate().add(coeff[j].duplicate().mul(BigInteger.valueOf(i).pow(j)).duplicate());
        }
        return res.duplicate();
    }
}
