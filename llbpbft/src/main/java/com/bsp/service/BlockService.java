package com.bsp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bsp.entity.Block;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Block服务
 * </p>
 *
 * @author zks
 * @since 2021-05-22
 */
@Service
public interface BlockService extends IService<Block> {
    /**
     * 在区块链上扩展新区块
     *
     * @param highBlock
     * @param curViewNum
     * @param editOptions
     * @param curAggrSig
     * @return
     */
    Block extendNewBlock(Block highBlock, Integer curViewNum, String editOptions, String curAggrSig);

    /**
     * 在区块链上扩展新区块
     *
     * @param highBlock
     * @param curViewNum
     * @param editOptions
     * @param curAggrSig
     * @return
     */
    Block genNewBlock(Block highBlock, Integer curViewNum, String editOptions, String curAggrSig);
    /**
     * 更新区块
     *
     * @param block
     */
    void update(Block block);

    /**
     * getParentBlock
     *
     * @param block
     */
    Block getParentBlock(Block block);

    /**
     * isSafeNewBlock
     *
     * @param block
     */
    Boolean isSafeNewBlock(Block block);

    /**
     * updateLocalStatus
     */
    void pullLocalStatus();

    /**
     * updateLocalStatus
     */
    void insertAndUpdateNewBlock(Block block);


}
