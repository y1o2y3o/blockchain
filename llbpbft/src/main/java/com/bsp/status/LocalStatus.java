package com.bsp.status;

import com.bsp.entity.Block;
import com.bsp.entity.Blockchain;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地变量表
 *
 * @author zksfromusa
 */
@Component
@Data
public class LocalStatus {
    private volatile Integer curViewNumber; // 当前视图
    private volatile Block lockedBlock; // 锁定的区块
    private volatile Block preparedBlock; // 最新准备的区块
    private volatile Block committedBlock; // 最新提交的区块
    private volatile Integer maxBlockHeight; // 最大区块高度

    /**
     * 视图-状态映射
     */
    private Map<Integer, State> viewStateMap = new HashMap<>();

    /**
     * 视图SyncHeightVotes映射
     */
    private Map<Integer, List<String>> viewSyncHeightVoteMap = new HashMap<>();

//    private Blockchain blockchain;

    private volatile Boolean syncFlag = false; // 同步进程
}
