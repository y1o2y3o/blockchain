package com.bsp.init;

import com.bsp.conf.ServerConfig;
import com.bsp.entity.Block;
import com.bsp.service.BlockService;
import com.bsp.service.MsgService;
import com.bsp.service.StatusService;
import com.bsp.signatures.ThresholdSignature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.bsp.web.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试
 */
@Component
@Slf4j
@Order(3333)
public class TestPrepare implements CommandLineRunner {
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

    @Override
    public void run(String... args) throws Exception {
        // 测试对于prepareBlock的部分签名
//        Block preparedBlock = localStatus.getPreparedBlock();
//        String pSig = signature.partialSign(preparedBlock);
//        List<String> urlList = globalStatus.getHostList().stream().map(url -> url + "/test/sig").collect(Collectors.toList());
//        msgService.broadcastPost(urlList, Message.builder().block(preparedBlock).partialSig(pSig).build()); // 发送pSig
    }
}
