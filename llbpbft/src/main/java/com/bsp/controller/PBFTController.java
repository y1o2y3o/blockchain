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
import com.bsp.web.Message;
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

/**
 * @author zksfromusa
 */
@RestController
@RequestMapping("/pbft/message")
@Slf4j
public class PBFTController {
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

    // 监听COMMIT
    @PostMapping("/COMMIT")
    public Result<?> handleProposalVote(@RequestBody @Valid Message msg) {
        log.info(String.format("接收到%s消息，当前状态为%s", "COMMIT", localStatus.toString()));

        int curViewNum = localStatus.getCurViewNumber(); // 当前视图
        Map<Integer, State> viewStateMap = localStatus.getViewStateMap();
        // 获得当前视图的状态
        State curState = viewStateMap.getOrDefault(curViewNum, new State());
        viewStateMap.put(curViewNum, curState);
        Block curBlock = blockService.getById(msg.getBlock().getBlockId()); // 数据库中的block
        // 若上一个视图的COMMIT消息 且 签名验证通过
        if (
                blockService.blockEquals(curBlock, msg.getBlock())
                        && msg.getViewNumber() + 1 <= curViewNum
                        && thresholdSignature.partialValidate(msg.getBlock(), msg.getPartialSig(), msg.getUrl())
        ) {
            // 收集签名
            Map<Long, Set<String>> bidPsigMap = curState.getBidPsigsMap();
            Set<String> pSigs = bidPsigMap.getOrDefault(msg.getBlock().getBlockId(), new HashSet<>());
            bidPsigMap.put(msg.getBlock().getBlockId(), pSigs);
            pSigs.add(msg.getPartialSig());
        } else {
            log.info("签名验证不通过!");
            return Result.failure(ResultStatus.BAD_REQUEST); // 签名验证不通过
        }

        // 如果收集满n-f个签名
        int n = statusService.curHostsNum();
        int f = statusService.curMaxFaultToleranceNum();
        Pair<Long, Set<String>> highBlockSigs = getHighBlockSigs(curState.getBidPsigsMap()); // 获得最高区块以及签名
        Block highBlock = blockService.getById(highBlockSigs.getKey()); // 获得最高区块
        highBlock.setFlag(null);
        List<String> sigs = new ArrayList<>(highBlockSigs.getValue()); // 最高区块的部分签名集合
        // 若收集满签名并更新curState
        if (curState.getCurAggrSig() == null && sigs.size() >= n - f) {
            synchronized (PBFTController.class) {
                if (curState.getCurAggrSig() == null && sigs.size() >= n - f) {
                    curState.setCurAggrSig(thresholdSignature.aggrSign(highBlock, sigs)); // 最高区块的签名
                    curState.setPartialSigList(sigs); // 更新当前签名集合
                    curState.setHighBlock(highBlock); // 更新当前最高区块
                    // 这里的聚合签名当作普通签名处理
                    Boolean verifyRes = thresholdSignature.aggrValidata(highBlock, curState.getCurAggrSig());
                    log.info(String.format("pbft sign签名自我验证结果:%s", verifyRes));
                }
            }

            return Result.success();
        }
        return Result.success();
    }

    //领导监听客户端请求
    @GetMapping("/REQUEST")
    public Result<?> handleEditRequest(@RequestParam("editOptions") String editOptions) throws UnsupportedEncodingException {
        editOptions = URLDecoder.decode(editOptions);
        msgService.get(serverConfig.getRegUrl() + "/status/confirmHighBlock2"); // 触发投票机制
        log.info(String.format("接收到%s消息内容:%s，当前状态为%s", "REQUEST", editOptions,localStatus.toString()));
        int curViewNum = localStatus.getCurViewNumber();
        if (!statusService.isCurrentLeader()) { // 不是主节点
            boolean success = msgService.testAndGet(statusService.leader(curViewNum) + "/pbft/message/REQUEST?editOptions=" + URLEncoder.encode(editOptions));
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
        // 广播PREPARE消息
        globalStatus.getHostList().forEach(host -> {
            msgService.post(host + "/pbft/message/PREPARE",
                    Message.builder()
                            .block(newBlock)
                            .viewNumber(curViewNum)
                            .url(serverConfig.getUrl())
                            .build());
        });
        return Result.success();
    }

    // 副本/领导监听PREPARE
    @PostMapping("/PREPARE")
    public Result<?> handleProposal(@RequestBody @Valid Message msg) {
        log.info(String.format("接收到%s消息，当前状态为%s", "PREPARE", localStatus.toString()));

        int curViewNum = localStatus.getCurViewNumber(); // 当前视图
        Map<Integer, State> viewStateMap = localStatus.getViewStateMap();
        // 获得当前视图的状态
        State curState = viewStateMap.getOrDefault(curViewNum, new State());
        viewStateMap.put(curViewNum, curState);

        // 如果签名合法
        Block parentBlock = blockService.getParentBlock(msg.getBlock());
        parentBlock.setFlag(null);
        String aggrSig = msg.getBlock().getAggrSig();
        Boolean aggrValidateRes = thresholdSignature.aggrValidata(parentBlock, aggrSig); // 签名认证结果
        log.info(String.format("区块:%s\n签名认证结果：%s", parentBlock, aggrValidateRes));
        if (thresholdSignature.aggrValidata(parentBlock, aggrSig)) {
            // 如果区块能够安全插入
            if (blockService.isSafeNewBlock(msg.getBlock())) {
                Block newBlock = msg.getBlock();
                // 对newBlock签名
                String pSig = thresholdSignature.partialSign(newBlock);
                blockService.insertAndUpdateNewBlock(newBlock);
                log.info(String.format("区块%s\n\t成功上链", newBlock));
                // 视图编号递增
                localStatus.setCurViewNumber(curViewNum + 1);
                return Result.success();
            }
        }
        return Result.failure(ResultStatus.CHANGE_VIEW);
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
