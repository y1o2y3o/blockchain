package com.bsp.controller;

import com.bsp.conf.ServerConfig;
import com.bsp.enums.MessageEnum;
import com.bsp.service.BlockService;
import com.bsp.service.MsgService;
import com.bsp.service.StatusService;
import com.bsp.signatures.ThresholdSignature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.bsp.status.State;
import com.bsp.web.Message;
import com.bsp.web.SyncMessage;
import com.csp.sig.SecretKeys;
import com.csp.web.Result;
import com.csp.web.ResultStatus;
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
import java.util.Map;
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

    @Autowired
    private ThresholdSignature thresholdSignature;

    @Autowired
    private BlockService blockService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private MsgService msgService;

    // 密钥状态更新
    @PostMapping("/update")
    public Result<?> update(@RequestBody Status status) {
        statusService.updateGlobalStatus(status);
        return Result.success();
    }

    // 获取当前highblock信息
    @GetMapping("/confirmHighBlock")
    public Result<?> getHignblockInfo() {
        msgService.confirmHighBlock(); // 给下一个领导发送highblock信息
        return Result.success();
    }
}
