package com.bsp.controller;

import com.bsp.conf.ServerConfig;
import com.bsp.entity.Block;
import com.bsp.service.BlockService;
import com.bsp.service.MsgService;
import com.bsp.signatures.Signature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.bsp.web.EditRequest;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

/**
 * @author zksfromusa
 */
@RestController
@RequestMapping("/outer")
@Slf4j
public class OuterController {
    @Autowired
    private ServerConfig serverConfig;
    @Autowired
    private LocalStatus localStatus;
    @Autowired
    private GlobalStatus globalStatus;

    @Autowired
    private MsgService msgService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private Signature signature;

    /**
     * 广播 操作上链请求
     *
     * @param operations
     * @return
     */
    @GetMapping("/req")
    public Result<?> handleReq(@RequestParam("op") String operations) {
        globalStatus.getHostList().forEach(hostUrl -> {
            msgService.post(hostUrl + "/msg/REQUEST", EditRequest.builder().editOptions(operations));
        });
        return Result.success();
    }

    /****************
     * 直接处理 操作上链请求
     *
     * @param operations
     * @return
     */
    @GetMapping("/directReq")
    public Result<?> handleDirectReq(@RequestParam("op") String operations) {
        Block block = blockService.genNewBlock(localStatus.getPreparedBlock(), localStatus.getMaxBlockHeight(), operations, null);
        block.setAggrSig(signature.sign(block));
        globalStatus.getHostList().forEach(hostUrl -> {


            msgService.post(hostUrl + "/outer/op", block);
        });
        return Result.success();
    }

    /******************
     * 监听 操作上链请求
     *
     * @param block
     * @return
     */
    @PostMapping("/op")
    public Result<?> handleOp(@RequestBody Block block) {
        blockService.insertAndUpdateNewBlock(block);
        return Result.success();
    }

}
