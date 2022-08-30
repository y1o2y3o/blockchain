package com.bsp.controller;

import com.bsp.conf.ServerConfig;
import com.bsp.entity.Block;
import com.bsp.service.BlockService;
import com.bsp.service.MsgService;
import com.bsp.service.StatusService;
import com.bsp.signatures.ThresholdSignature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.bsp.web.Message;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zksfromusa
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BlockService blockService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private GlobalStatus globalStatus;

    @Autowired
    private LocalStatus localStatus;

    @Autowired
    private ThresholdSignature signature;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private MsgService msgService;


    List<String> sigs = new ArrayList<>();

    @GetMapping("/startSig")
    public Result<?> startSig() {
        List<String> urlList = globalStatus.getHostList().stream().map(url -> url + "/test/sign").collect(Collectors.toList());
        msgService.broadcastGet(urlList);
        return Result.success();
    }

    @GetMapping("/sign")
    public Result<?> sign() {
        // 测试对于prepareBlock的部分签名
        Block preparedBlock = localStatus.getPreparedBlock();
        String pSig = signature.partialSign(preparedBlock);
        List<String> urlList = globalStatus.getHostList().stream().map(url -> url + "/test/validate").collect(Collectors.toList());
        msgService.broadcastPost(urlList, Message.builder().url(serverConfig.getUrl()).block(preparedBlock).partialSig(pSig).build()); // 发送pSig
        return Result.success();
    }

    // 密钥状态更新
    @PostMapping("/validate")
    public Result<?> validate(@RequestBody Message msg) {
        String pSig = msg.getPartialSig();
        log.info("收到 " + msg.getUrl() + " 部分签名：" + pSig);
        log.info(msg.getUrl() + " 部分签名认证结果：" + signature.partialValidate(msg.getBlock(), msg.getPartialSig(), msg.getUrl()).toString());
        sigs.add(pSig);
        int n = globalStatus.getHostList().size();
        int t = ParamComputing.getT(n);
//        if (sigs.size() >= t) { // 达到门限t
        String aggrSig = signature.aggrSign(msg.getBlock(), sigs);
        log.info("聚合签名认证结果[size" + sigs.size() + "]：" + signature.aggrValidata(msg.getBlock(), aggrSig));
        if (sigs.size() >= n) {
            sigs.clear(); // clear签名数组
        }
//        }

        return Result.success();
    }

}
