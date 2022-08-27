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
import com.bsp.status.State;
import com.bsp.web.*;
import com.csp.web.Result;
import com.csp.web.ResultStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;
import java.util.Objects;

/**
 * @author zksfromusa
 */
@RestController
@RequestMapping("/message")
public class MessageController {
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

    // 领导监听PROPOSAL_VOTE
    @PostMapping("/PROPOSAL_VOTE")
    public Result<?> handleProposalVote(@RequestBody @Valid Message msg) {
        if (!statusService.isCurrentLeader()) { // 不是领导
            return Result.failure(ResultStatus.NOT_LEADER);
        }
        int curViewNum = localStatus.getCurViewNumber();
        Map<Integer, State> viewStateMap = localStatus.getViewStateMap();
        // 获得当前视图的状态
        State curState = viewStateMap.getOrDefault(curViewNum, State.builder().build());
        viewStateMap.putIfAbsent(curViewNum, curState);
        // 若部分签名验证通过
        if (Objects.equals(msg.getViewNumber(), curViewNum - 1)
                && thresholdSignature.partialValidate(msg.getBlock(), msg.getPartialSig(), msg.getUrl())) {
            // 收集部分签名
            curState.getPartialSigList().add(msg.getPartialSig());
        } else{
            return Result.failure(ResultStatus.BAD_REQUEST); // 签名验证不通过
        }

        // 如果收集满n-f个签名
        int n = statusService.curHostsNum();
        int f = statusService.curMaxFaultToleranceNum();
        // 若收集满则聚合签名并更新curState
        if (curState.getPartialSigList().size() >= n - f) {
            curState.setCurAggrSig(thresholdSignature.aggrSign(msg.getBlock(), curState.getPartialSigList()));
            // 若发现更高区块则更新当前最高区块
            if (curState.getHighBlock() == null || msg.getBlock().getHeight() > curState.getHighBlock().getHeight()) {
                curState.setHighBlock(msg.getBlock());
            }
            // 区块高度不一致
            if(blockService.getById(curState.getHighBlock().getBlockId()) == null){
                SyncMessage syncMessage = SyncMessage.builder()
                        .viewNumber(curViewNum)
                        .host(msg.getUrl())
                        .blockId1(localStatus.getCommittedBlock().getBlockId())
                        .blockId2(curState.getHighBlock().getBlockId()).build();
                // 广播SYNC_HEIGHT消息
                globalStatus.getHostList().forEach(host -> {
                    if (!serverConfig.getUrl().equals(host)) { // 不包括自己
                        msgService.post(host + "message/" + MessageEnum.SYNC_HEIGHT.toString(), syncMessage);
                    }
                });
            }
        }
        return Result.success();
    }

    //领导监听客户端请求
    @PostMapping("/REQUEST")
    public Result<?> handleEditRequest(@RequestBody @Valid EditRequest req) {
        if (!statusService.isCurrentLeader()) { // 不是领导
            return Result.failure(ResultStatus.NOT_LEADER);
        }
        if(localStatus.getSyncFlag()){ // 正在同步区块高度
            return Result.failure(ResultStatus.SYNC_PROCESS);
        }
        int curViewNum = localStatus.getCurViewNumber();
        Map<Integer, State> viewStateMap = localStatus.getViewStateMap();
        // 获得当前视图的状态
        State curState = viewStateMap.getOrDefault(curViewNum, State.builder().build());
        viewStateMap.putIfAbsent(curViewNum, curState);
        if (curState.getHighBlock() == null) { // 尚未收到超过n-f个合法的投票
            return Result.failure(ResultStatus.NOT_READY);
        }
        // 对当前最高区块的聚合签名
        String curAggrSig = curState.getCurAggrSig();
        Block newBlock = blockService.extendNewBlock(curState.getHighBlock(), curViewNum, req.getEditOptions(), curAggrSig);
        blockService.update(newBlock); // 更新当前区块链状态
        localStatus.setCurViewNumber(curViewNum + 1); // 视图编号递增
        // 广播PROPOSAL消息
        globalStatus.getHostList().forEach(host -> {
            if (!serverConfig.getUrl().equals(host)) { // 不包括自己
                msgService.post(host + "message/" + MessageEnum.PROPOSAL.toString(),
                        Message.builder()
                                .block(newBlock)
                                .viewNumber(curViewNum)
                                .url(serverConfig.getUrl())
                                .build());
            }
        });
        return Result.success();
    }

    // 副本监听PROPOSAL
    @PostMapping("/PROPOSAL")
    public Result<?> handleProposal(@RequestBody @Valid Message msg) {
        if (!statusService.isCurrentReplica()) { // 不是副本
            return Result.failure(ResultStatus.NOT_REPLICA);
        }
        if (!statusService.leader(msg.getViewNumber()).equals(msg.getUrl())) { // 冒充领导
            return Result.failure(ResultStatus.BAD_REQUEST);
        }

        int curViewNum = localStatus.getCurViewNumber();
        Map<Integer, State> viewStateMap = localStatus.getViewStateMap();
        // 获得当前视图的状态
        State curState = viewStateMap.getOrDefault(curViewNum, State.builder().build());
        viewStateMap.putIfAbsent(curViewNum, curState);

        // 如果聚合签名合法
        if (thresholdSignature.aggrValidata(blockService.getParentBlock(msg.getBlock()), msg.getBlock().getAggrSig())) {
            // 如果区块能够安全插入
            if (blockService.isSafeNewBlock(msg.getBlock())) {
                Block newBlock = msg.getBlock();
                // 对newBlock部分签名
                String pSig = thresholdSignature.partialSign(newBlock);
                blockService.save(newBlock); // 插入区块
                blockService.update(newBlock); // 更新本地区块链状态
                // 对下一个视图的领导发送投票消息，其中包含该区块的部分签名
                msgService.post(statusService.leader(curViewNum + 1) + "message/" + MessageEnum.PROPOSAL_VOTE.toString(),
                        Message.builder()
                                .block(newBlock)
                                .viewNumber(curViewNum + 1)
                                .url(serverConfig.getUrl())
                                .partialSig(pSig)
                                .build());
                localStatus.setCurViewNumber(curViewNum + 1); // 视图编号递增
                return Result.success();
            }
        }
        // 聚合签名不通过，切换视图
        Block preparedBlock = localStatus.getPreparedBlock();
        String pSig = thresholdSignature.partialSign(preparedBlock);
        // 向下一个视图的领导节点发送切换视图消息，包含当前准备区块以及签名
        msgService.post(statusService.leader(curViewNum + 1) + "message/" + MessageEnum.CHANGE_VIEW.toString(),
                Message.builder()
                        .type(MessageEnum.CHANGE_VIEW.toString())
                        .block(preparedBlock)
                        .viewNumber(curViewNum + 1)
                        .url(serverConfig.getUrl())
                        .partialSig(pSig)
                        .build());
        localStatus.setCurViewNumber(curViewNum + 1); // 视图编号递增
        return Result.failure(ResultStatus.CHANGE_VIEW);
    }

    // 被指定的领导监听CHANGE_VIEW
    @PostMapping("/CHANGE_VIEW")
    public Result<?> handleViewChange(@RequestBody @Valid Message msg) {
        if (!statusService.leader(msg.getViewNumber()).equals(serverConfig.getUrl())) { // 不是指定的领导
            return Result.failure(ResultStatus.NOT_LEADER);
        }
        // 验证 CHANGE_VIEW 消息中的部分签名份额 若成功则改变视图序号
        if (localStatus.getCurViewNumber() <= msg.getViewNumber() && thresholdSignature.partialValidate(msg.getBlock(), msg.getPartialSig(), msg.getUrl())) {
            //;
            Map<Integer, State> viewStateMap = localStatus.getViewStateMap();
            // 获得指定的视图的状态
            State viewState = viewStateMap.getOrDefault(msg.getViewNumber(), State.builder().build());
            viewStateMap.putIfAbsent(msg.getViewNumber(), viewState);
            viewState.getPartialSigList().add(msg.getPartialSig());
            // 如果收集满n-f个签名
            int n = statusService.curHostsNum();
            int f = statusService.curMaxFaultToleranceNum();
            // 若收集满则聚合签名并更新curState
            if (viewState.getPartialSigList().size() >= n - f) {
                localStatus.setCurViewNumber(msg.getViewNumber()); // 宣称为领导
                // 生成聚合签名
                viewState.setCurAggrSig(thresholdSignature.aggrSign(msg.getBlock(), viewState.getPartialSigList()));
                // 若发现更高区块则更新当前最高区块
                if (viewState.getHighBlock() == null || msg.getBlock().getHeight() > viewState.getHighBlock().getHeight()) {
                    viewState.setHighBlock(msg.getBlock());
                }
            }
        }
        return Result.success();
    }


}
