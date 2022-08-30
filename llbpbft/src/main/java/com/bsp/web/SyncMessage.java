package com.bsp.web;

import com.bsp.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * SyncMessage消息
 *
 * @author zksfromusa
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncMessage {
    private String type;

    @NotNull
    private Integer viewNumber;

    @NotNull
    private String host; // 把区块送出的送出节点
    @NotNull
    private Long blockId1;
    @NotNull
    private Long blockId2;

    private String partialSig;
    private String aggrSig;

    @NotNull
    private String url;


}
