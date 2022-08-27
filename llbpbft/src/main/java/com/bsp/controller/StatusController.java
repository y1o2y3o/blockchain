package com.bsp.controller;

import com.bsp.conf.ServerConfig;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.csp.sig.SecretKeys;
import com.csp.web.Result;
import com.rsp.status.Status;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author zksfromusa
 */
@RestController
@RequestMapping("/status")
@Slf4j
public class StatusController {
    @Autowired
    private ServerConfig serverConfig;
    @Autowired
    private LocalStatus localStatus;
    @Autowired
    private GlobalStatus globalStatus;


    // 密钥状态更新
    @PostMapping("/update")
    public Result<?> update(@RequestBody Status status) {
        globalStatus.setHostList(status.getHostList());
        Pairing bp = PairingFactory.getPairing("llbpbft/a.properties");
        Field G1 = bp.getG1();
        Field Zr = bp.getZr();
        Element g = G1.newElementFromBytes(Base64.decode(status.getG()));
        Element SK = Zr.newElementFromBytes(Base64.decode(status.getSk()));
        Element PK = G1.newElementFromBytes(Base64.decode(status.getPk()));
        int n = status.getHostList().size();
        status.getHostList().forEach(hostUrl -> {
            Element partialSK = Zr.newElementFromBytes(Base64.decode(status.getHostSecretKeyMap().get(hostUrl)));
            Element partialPK = G1.newElementFromBytes(Base64.decode(status.getHostPubKeyMap().get(hostUrl)));
            SecretKeys secretKeys = SecretKeys.builder()
                    .bp(bp)
                    .G1(G1)
                    .Zr(Zr)
                    .g(g)
                    .SK(SK)
                    .PK(PK)
                    .partialPK(partialPK)
                    .partialSK(partialSK)
                    .build();
            globalStatus.getHostSecretKeyMap().put(hostUrl, secretKeys);
            // 若当前状态为自己
            if (Objects.equals(hostUrl, serverConfig.getUrl())) {
                globalStatus.setSecretKeys(secretKeys);
            }
        });
        log.info(globalStatus.toString());
        return Result.success();
    }

}
