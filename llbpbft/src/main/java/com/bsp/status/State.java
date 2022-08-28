package com.bsp.status;

import com.bsp.entity.Block;
import com.csp.util.ParamComputing;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class State {
    /**
     * 当前部分签名集合
     */
    private List<String> partialSigList = null;

    /**
     * 当前block_id -> 部分签名list 映射
     */
    private Map<Long, Set<String>> bidPsigsMap = new HashMap<>();

    /**
     * 当前最高区块
     */
    private volatile Block highBlock = null;

    /**
     * 当前聚合签名
     */
    private volatile String curAggrSig = null;
}
