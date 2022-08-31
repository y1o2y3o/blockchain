package com.bsp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 区块
 *
 * @author zksfromusa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "block", resultMap = "blockResultMap")
public class Block implements Comparable<Block> {
    @TableId("block_id")
    private Long blockId; // 全局唯一ID

    @TableField("view_number")
    private Integer viewNumber; // 视图序号

    @TableField("height")
    private Integer height; // 区块高度

    @TableField("parent_block_id")
    private Long parentBlockId; // 父节点ID

    @TableField("parent_hash")
    private String parentHash; // 父节点摘要

    @TableField("content")
    private String content; // 交易内容

    @TableField("hash")
    private String hash; // 摘要 = genHash(id + viewNumber + height + parentId + parentHash + content)

    @TableField("aggr_sig")
    private String aggrSig; // 当前视图对父区块(viewNumber + parentHash)的聚合签名

    @TableField("flag")
    private String flag; // 状态COMMITTED LOCKED PREPARED

    @Override
    public int compareTo(Block o) {
        return Long.compare(this.getBlockId(), o.getBlockId());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Block) {
            Block b = (Block) o;
            return Objects.equals(b.blockId, ((Block) o).blockId)
                    && Objects.equals(b.getContent(), ((Block) o).getContent());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return blockId.hashCode();
    }
}
