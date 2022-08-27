package com.bsp.controller;

import com.alibaba.fastjson.JSON;
import com.bsp.conf.ServerConfig;
import com.bsp.entity.Block;
import com.bsp.enums.MessageEnum;
import com.bsp.service.BlockService;
import com.bsp.service.MsgService;
import com.bsp.service.StatusService;
import com.bsp.signatures.ThresholdSignature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.bsp.status.State;
import com.bsp.web.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author zksfromusa
 */
@RestController
@RequestMapping("/syncMessage")
public class SyncMessageController {
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

    // 副本监听SYNC_HEIGHT消息
    @PostMapping("/SYNC_HEIGHT")
    public Result<?> handleSyncHeight(@RequestBody @Valid SyncMessage msg) {
        if (!statusService.isCurrentReplica()) { // 不是副本
            return Result.failure(ResultStatus.NOT_REPLICA);
        }
        if (!statusService.isLeader(msg.getUrl(), msg.getViewNumber())) { // 不是领导发来的
            return Result.failure(ResultStatus.NOT_LEADER);
        }
        int curViewNum = localStatus.getCurViewNumber();
        if (curViewNum == msg.getViewNumber()) { // 当前视图
            Block block1 = blockService.getById(msg.getBlockId1());
            Block block2 = blockService.getById(msg.getBlockId2());
            // 合法的区块
            if (block1 != null && block2 != null && block2.getHeight() >= block1.getHeight()) {
                String sig = thresholdSignature.partialSign(
                        SyncMessage.builder()
                                .viewNumber(msg.getViewNumber())
                                .blockId1(msg.getBlockId1())
                                .blockId2(msg.getBlockId2())
                                .host(msg.getHost())
                                .build());// 对消息签名
                //// 向领导指定的副本 发送开始同步消息
                msgService.post(msg.getHost() + "/syncMessage" + MessageEnum.SYNC_HEIGHT_VOTE.toString(),
                        SyncMessage.builder()
                                .viewNumber(curViewNum)
                                .host(msg.getHost())
                                .blockId1(msg.getBlockId1())
                                .blockId2(msg.getBlockId2())
                                .partialSig(sig)
                );

            }
        }
        return Result.success();
    }

    // 副本监听SYNC_HEIGHT_VOTE消息
    @PostMapping("/SYNC_HEIGHT_VOTE")
    public Result<?> handleSyncHeightVote(@RequestBody @Valid SyncMessage msg) {
        if (!statusService.isCurrentReplica()) { // 不是副本
            return Result.failure(ResultStatus.NOT_REPLICA);
        }

        int curViewNum = localStatus.getCurViewNumber();
        if (curViewNum == msg.getViewNumber()) { // 当前视图
            String sig = msg.getPartialSig();
            List<String> viewVoteSigs = localStatus.getViewSyncHeightVoteMap().get(curViewNum);
            if (thresholdSignature.partialValidate(
                    SyncMessage.builder()
                            .viewNumber(msg.getViewNumber())
                            .blockId1(msg.getBlockId1())
                            .blockId2(msg.getBlockId2())
                            .host(msg.getHost())
                            .build()
                    , sig
                    , msg.getUrl())) {
                viewVoteSigs.add(sig);
            }
            // 如果收集满n-f个签名
            int n = statusService.curHostsNum();
            int f = statusService.curMaxFaultToleranceNum();
            // 若收集满则开始传输
            if (viewVoteSigs.size() >= n - f) {
                Stack<Block> stack = new Stack<>();
                ArrayList<Block> blockList = new ArrayList<>();
                Block block1 = blockService.getById(msg.getBlockId1());
                Block block2 = blockService.getById(msg.getBlockId2());
                // 合法的边界
                if (block1 != null && block2 != null && block2.getHeight() >= block1.getHeight()) {
                    Block target = block2;
                    while (!Objects.equals(target.getBlockId(), block1.getBlockId())) {
                        stack.push(target);
                        target = blockService.getById(target.getParentBlockId());
                    }
                    stack.push(target);
                    // 调换顺序
                    while (!stack.isEmpty()) {
                        blockList.add(stack.pop());
                    }

                    // 向领导同步区块队列
                    msgService.post(statusService.leader(curViewNum) + "syncMessage/" + MessageEnum.UPLOAD_BLOCKS, blockList);
                }

            }
        }
        return Result.success();
    }

    // 领导监听UPLOAD_BLOCKS消息
    @PostMapping("/UPLOAD_BLOCKS")
    public Result<?> handleUploadBlocks(@RequestBody @Valid List<Block> blockList) {
        if (localStatus.getSyncFlag() && statusService.isCurrentLeader()) { // 是领导且正在同步
            blockService.saveOrUpdateBatch(blockList);
            localStatus.setSyncFlag(false); // 关闭同步标记
            return Result.success();
        }
        return Result.failure("UPLOAD_BLOCKS同步失败");

    }
}
