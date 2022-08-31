package com.bsp.controller;

import com.bsp.conf.ServerConfig;
import com.bsp.entity.Block;
import com.bsp.enums.MessageEnum;
import com.bsp.service.BlockService;
import com.bsp.service.MsgService;
import com.bsp.service.StatusService;
import com.bsp.signatures.ThresholdSignature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.bsp.web.SyncHeightRespose;
import com.bsp.web.SyncMessage;
import com.csp.web.Result;
import com.csp.web.ResultStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

/**
 * 高度同步
 *
 * @author zksfromusa
 */
@RestController
@RequestMapping("/syncHeightMessage")
public class SyncHeightMessageController {
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

    @Autowired
    private ServerConfig serverConfig;

    // 监听SYNC_HEIGHT消息
    @GetMapping("/SYNC_HEIGHT")
    public SyncHeightRespose getBlockGtHeight(@RequestParam("height") @NotNull Integer height) {
        List<Block> blockList = blockService.getFollowingBlocksAfter(height);
        return SyncHeightRespose.builder().blockList(blockList).build();
    }
}
