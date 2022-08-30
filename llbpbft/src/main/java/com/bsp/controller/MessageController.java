package com.bsp.controller;

import com.bsp.conf.ServerConfig;
import com.bsp.entity.Block;
import com.bsp.enums.FlagEnum;
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
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zksfromusa
 */
@RestController
@RequestMapping("/message")
@Slf4j
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
        log.info(String.format("接收到%s消息，当前状态为%s", "PROPOSAL_VOTE", localStatus.toString()));
        if (!statusService.isCurrentLeader()) { // 不是领导
            return Result.failure(ResultStatus.NOT_LEADER);
        }
        int curViewNum = localStatus.getCurViewNumber(); // 当前视图
        Map<Integer, State> viewStateMap = localStatus.getViewStateMap();
        // 获得当前视图的状态
        State curState = viewStateMap.getOrDefault(curViewNum, new State());
        viewStateMap.put(curViewNum, curState);
        Block curBlock = blockService.getById(msg.getBlock().getBlockId()); // 数据库中的block
        // 若上一个视图的PROPOSAL_VOTE消息 且 部分签名验证通过
        if (
                blockService.blockEquals(curBlock, msg.getBlock())
                        && msg.getViewNumber() + 1 <= curViewNum
                        && thresholdSignature.partialValidate(msg.getBlock(), msg.getPartialSig(), msg.getUrl())
        ) {
            // 收集部分签名
            Map<Long, Set<String>> bidPsigMap = curState.getBidPsigsMap();
            Set<String> pSigs = bidPsigMap.getOrDefault(msg.getBlock().getBlockId(), new HashSet<>());
            bidPsigMap.put(msg.getBlock().getBlockId(), pSigs);
            pSigs.add(msg.getPartialSig());
        } else {
            log.info("部分签名验证不通过!");
            return Result.failure(ResultStatus.BAD_REQUEST); // 签名验证不通过
        }

        // 如果收集满n-f个签名
        int n = statusService.curHostsNum();
        int f = statusService.curMaxFaultToleranceNum();
        Pair<Long, Set<String>> highBlockSigs = getHighBlockSigs(curState.getBidPsigsMap()); // 获得最高区块以及签名
        Block highBlock = blockService.getById(highBlockSigs.getKey()); // 获得最高区块
        highBlock.setFlag(null);
        List<String> sigs = new ArrayList<>(highBlockSigs.getValue()); // 最高区块的部分签名集合
        // 若收集满则聚合签名并更新curState
        if (curState.getCurAggrSig() == null && sigs.size() >= n - f) {
            synchronized (MessageController.class) {
                if (curState.getCurAggrSig() == null && sigs.size() >= n - f) {
                    curState.setCurAggrSig(thresholdSignature.aggrSign(highBlock, sigs)); // 最高区块的聚合签名
                    curState.setPartialSigList(sigs); // 更新当前部分签名集合
                    curState.setHighBlock(highBlock); // 更新当前最高区块
                    Boolean verifyRes = thresholdSignature.aggrValidata(highBlock, curState.getCurAggrSig());

                    log.info(String.format("aggrSign签名为:%s，区块已经确认", curState.getCurAggrSig()));
                    log.info(String.format("aggrSign签名自我验证结果:%s", verifyRes));
                }
            }

//            // 若发现更高区块则更新当前最高区块
//            if (curState.getHighBlock() == null || msg.getBlock().getHeight() > curState.getHighBlock().getHeight()) {
//                curState.setHighBlock(msg.getBlock());
//            }
//            // 区块高度不一致
//            if (blockService.getById(curState.getHighBlock().getBlockId()) == null) {
//                SyncMessage syncMessage = SyncMessage.builder()
//                        .viewNumber(curViewNum)
//                        .host(msg.getUrl())
//                        .blockId1(localStatus.getCommittedBlock().getBlockId())
//                        .blockId2(curState.getHighBlock().getBlockId()).build();
//                // 广播SYNC_HEIGHT消息
//                globalStatus.getHostList().forEach(host -> {
//                    if (!serverConfig.getUrl().equals(host)) { // 不包括自己
//                        msgService.post(host + "message/" + MessageEnum.SYNC_HEIGHT.toString(), syncMessage);
//                    }
//                });
//            }

            return Result.success();
        }
        return Result.success();
    }

    //领导监听客户端请求
    @GetMapping("/REQUEST")
    public Result<?> handleEditRequest(@RequestParam("editOptions") String editOptions) throws UnsupportedEncodingException {
        editOptions = URLDecoder.decode(editOptions);
        msgService.get(serverConfig.getRegUrl() + "/status/confirmHighBlock"); // 触发投票机制
        log.info(String.format("接收到%s消息内容:%s，当前状态为%s", "REQUEST", editOptions,localStatus.toString()));
        int curViewNum = localStatus.getCurViewNumber();
        if (!statusService.isCurrentLeader()) { // 不是领导
            boolean success = msgService.testAndGet(statusService.leader(curViewNum) + "/message/REQUEST?editOptions=" + URLEncoder.encode(editOptions));
            log.info("正在将用户请求 " + editOptions + " 转发给当前视图[" + curViewNum + "]领导 " + statusService.leader(curViewNum));
            return success ? Result.success(ResultStatus.REDIRECT) : Result.failure(ResultStatus.INTERNAL_SERVER_ERROR);
        }
//        if (localStatus.getSyncFlag()) { // 正在同步区块高度
//            return Result.failure(ResultStatus.SYNC_PROCESS);
//        }

        Map<Integer, State> viewStateMap = localStatus.getViewStateMap();
        // 获得当前视图的状态
        State curState = viewStateMap.getOrDefault(curViewNum, new State());
        viewStateMap.put(curViewNum, curState);
        if (curState.getHighBlock() == null) { // 尚未收到超过n-f个合法的投票
            return Result.failure(ResultStatus.NOT_READY);
        }
        // 对当前最高区块的聚合签名
        String curAggrSig = curState.getCurAggrSig();
        // 插入新区块
        Block newBlock = blockService.genNewBlock(curState.getHighBlock(), curViewNum, editOptions, curAggrSig);
        //blockService.update(newBlock); // 更新当前区块链状态
        // 广播PROPOSAL消息
        globalStatus.getHostList().forEach(host -> {
            msgService.post(host + "/message/" + MessageEnum.PROPOSAL.toString(),
                    Message.builder()
                            .block(newBlock)
                            .viewNumber(curViewNum)
                            .url(serverConfig.getUrl())
                            .build());
        });
        return Result.success();
    }

    // 副本/领导监听PROPOSAL(当前领导发来的)
    @PostMapping("/PROPOSAL")
    public Result<?> handleProposal(@RequestBody @Valid Message msg) {
        log.info(String.format("接收到%s消息，当前状态为%s", "PROPOSAL", localStatus.toString()));
        // 冒充领导
        if (!statusService.leader(msg.getViewNumber()).equals(msg.getUrl())) {
            return Result.failure(ResultStatus.BAD_REQUEST);
        }

        int curViewNum = localStatus.getCurViewNumber(); // 当前视图
        Map<Integer, State> viewStateMap = localStatus.getViewStateMap();
        // 获得当前视图的状态
        State curState = viewStateMap.getOrDefault(curViewNum, new State());
        viewStateMap.put(curViewNum, curState);

        // 如果聚合签名合法
        Block parentBlock = blockService.getParentBlock(msg.getBlock());
        parentBlock.setFlag(null);
        String aggrSig = msg.getBlock().getAggrSig();
        Boolean aggrValidateRes = thresholdSignature.aggrValidata(parentBlock, aggrSig); // 聚合签名认证结果
        log.info(String.format("区块:%s\n聚合签名认证结果：%s", parentBlock, aggrValidateRes));
        if (thresholdSignature.aggrValidata(parentBlock, aggrSig)) {
            // 如果区块能够安全插入
            if (blockService.isSafeNewBlock(msg.getBlock())) {
                Block newBlock = msg.getBlock();
                // 对newBlock部分签名
                String pSig = thresholdSignature.partialSign(newBlock);
                blockService.insertAndUpdateNewBlock(newBlock);
                log.info(String.format("区块%s\n\t成功上链", newBlock));
                // 视图编号递增
                localStatus.setCurViewNumber(curViewNum + 1);
//                // 对下一个视图的领导发送投票消息，其中包含该区块的部分签名
//                msgService.post(statusService.leader(curViewNum + 1) + "/message/" + MessageEnum.PROPOSAL_VOTE.toString(), // 下一个视图的领导
//                        Message.builder()
//                                .block(newBlock)
//                                .viewNumber(curViewNum) // 原来的视图
//                                .url(serverConfig.getUrl())
//                                .partialSig(pSig)
//                                .build());
                return Result.success();
            }
        }
//        // 聚合签名不通过，切换视图
//        Block preparedBlock = localStatus.getPreparedBlock();
//        String pSig = thresholdSignature.partialSign(preparedBlock);
//        // 向下一个视图的领导节点发送切换视图消息，包含当前准备区块以及签名
//        msgService.post(statusService.leader(curViewNum + 1) + "message/" + MessageEnum.CHANGE_VIEW.toString(),
//                Message.builder()
//                        .type(MessageEnum.CHANGE_VIEW.toString())
//                        .block(preparedBlock)
//                        .viewNumber(curViewNum + 1)
//                        .url(serverConfig.getUrl())
//                        .partialSig(pSig)
//                        .build());
//        localStatus.setCurViewNumber(curViewNum + 1); // 视图编号递增
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
            State viewState = viewStateMap.getOrDefault(msg.getViewNumber(), new State());
            viewStateMap.put(msg.getViewNumber(), viewState);
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

    // 客户端查询接口
    @GetMapping("/GET")
    public Result<?> getEditResponse(@RequestParam("curHeight") @NotNull Integer curHeight) {
        List<String> opList = blockService.getFollowingOptionsListAfter(curHeight);
        return Result.success(opList);
    }

    /**
     * 获取getHighBlockSigs
     *
     * @param bidPsigsMap
     * @return
     */
    private Pair<Long, Set<String>> getHighBlockSigs(Map<Long, Set<String>> bidPsigsMap) {
        int maxSize = bidPsigsMap.values().stream()
                .map(Set::size)
                .max((s1, s2) -> Integer.compare(s2, s1))
                .orElse(0);
        Map.Entry<Long, Set<String>> res = bidPsigsMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() == maxSize)
                .findAny()
                .orElse(null);
        assert res != null;
        return new Pair<>(res.getKey(), new HashSet<>(res.getValue()));
    }
}
