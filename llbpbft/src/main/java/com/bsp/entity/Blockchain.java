package com.bsp.entity;

import lombok.Data;

/**
 * 区块链
 * @author zksfromusa
 */
@Data
public class Blockchain {
    private String id; // 全局唯一ID
    private Integer viewNumber; // 视图序号
    private Integer height; // 区块高度
    private String parentId; // 父节点ID
    private String parentHash; // 父节点摘要
    private String content; // 交易内容
    private String hash; // 摘要 = genHash(id + viewNumber + height + parentId + parentHash + content)
    private String aggrSig; // 当前视图对父区块(viewNumber + parentHash)的聚合签名
}
