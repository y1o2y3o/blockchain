package com.bsp.web;

import com.bsp.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 消息
 *
 * @author zksfromusa
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String type;

    @NotNull
    private Integer viewNumber;
    private Block block;
    private String partialSig;
    private String aggrSig;
    @NotNull
    private String url;
}
