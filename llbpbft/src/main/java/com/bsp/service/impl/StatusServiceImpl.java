package com.bsp.service.impl;

import com.bsp.conf.ServerConfig;
import com.bsp.exceptions.CommonException;
import com.bsp.service.BlockService;
import com.bsp.service.MsgService;
import com.bsp.service.StatusService;
import com.bsp.signatures.ThresholdSignature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.csp.sig.SecretKeys;
import com.csp.util.ParamComputing;
import com.csp.web.Result;
import com.rsp.status.Status;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
@Slf4j
public class StatusServiceImpl implements StatusService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BlockService blockService;
    @Autowired
    private MsgService msgService;

    @Autowired
    private GlobalStatus globalStatus;

    @Autowired
    private LocalStatus localStatus;

    @Autowired
    private ThresholdSignature signature;

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

    @Override
    public Status getStatus() {
        // 访问下一个节点
        return Objects.requireNonNull(restTemplate.getForObject(serverConfig.getRegUrl() + "/status/getStatus", Status.class));
    }

    @Override
    public void updateGlobalStatus(Status status) {
        globalStatus.setHostList(status.getHostList());
        Pairing bp = PairingFactory.getPairing("llbpbft/a.properties");
        Field G1 = bp.getG1();
        Field Zr = bp.getZr();
        Element g = G1.newElementFromBytes(Base64.decode(status.getG())).duplicate();
        Element SK = Zr.newElementFromBytes(Base64.decode(status.getSk())).duplicate();
        Element PK = G1.newElementFromBytes(Base64.decode(status.getPk())).duplicate();
        int n = status.getHostList().size();
        status.getHostList().forEach(hostUrl -> {
            Element partialSK = Zr.newElementFromBytes(Base64.decode(status.getHostSecretKeyMap().get(hostUrl))).duplicate();
            Element partialPK = G1.newElementFromBytes(Base64.decode(status.getHostPubKeyMap().get(hostUrl))).duplicate();
            SecretKeys secretKeys = SecretKeys.builder()
                    .bp(bp)
                    .G1(G1)
                    .Zr(Zr)
                    .g(g.duplicate())
                    .SK(SK.duplicate())
                    .PK(PK.duplicate())
                    .partialPK(partialPK.duplicate())
                    .partialSK(partialSK.duplicate())
                    .build();
            globalStatus.getHostSecretKeyMap().put(hostUrl, secretKeys);
            // 若当前状态为自己
            if (Objects.equals(hostUrl, serverConfig.getUrl())) {
                globalStatus.setSecretKeys(secretKeys);
            }
        });
        log.info(globalStatus.toString());
    }

    @Override
    public void getAndUpdateGlobalStatus() {
        Status status = getStatus();
        if (status == null) {
            throw new CommonException("status获取失败");
        }
        updateGlobalStatus(status);
    }


}
