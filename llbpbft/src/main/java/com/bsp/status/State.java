package com.bsp.status;

import com.bsp.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class State {
    /**
     * 当前部分签名集合
     */
    private List<String> partialSigList = new ArrayList<>();

    /**
     * 当前最高区块
     */
    private Block highBlock = null;

    /**
     * 当前聚合签名
     */
    private String curAggrSig = null;
}
