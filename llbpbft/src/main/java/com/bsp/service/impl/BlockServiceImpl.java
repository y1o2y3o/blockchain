package com.bsp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bsp.dao.BlockDao;
import com.bsp.entity.Block;
import com.bsp.exceptions.CommonException;
import com.bsp.service.BlockService;
import com.bsp.status.LocalStatus;
import com.bsp.utils.Hashing;
import com.bsp.utils.SnowFlakeIdUtil;
import com.csp.web.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Block服务实现类
 * </p>
 *
 * @author zks
 * @since 2021-05-22
 */
@Service
@Slf4j
public class BlockServiceImpl extends ServiceImpl<BlockDao, Block> implements BlockService {

    @Autowired
    private LocalStatus localStatus;

    @Autowired
    private BlockDao blockDao;

    @Override
    @Transactional(rollbackFor = {RuntimeException.class})
    public Block extendNewBlock(Block highBlock, Integer curViewNum, String editOptions, String curAggrSig) {
        Block newBlock = Block.builder()
                .blockId(SnowFlakeIdUtil.getSnowflakeId())
                .viewNumber(curViewNum)
                .content(editOptions)
                .aggrSig(curAggrSig)
                .parentBlockId(highBlock.getBlockId())
                .parentHash(highBlock.getHash())
                .height(highBlock.getHeight() + 1)
                .build();
        String hash = Hashing.genHashDigest(newBlock);
        newBlock.setHash(hash);
        if (!this.save(newBlock)) {
            throw new CommonException(ResultStatus.BLOCK_INSERT_ERROR);
        }
        return newBlock;
    }

    @Override
    public void update(Block block) {
        synchronized (LocalStatus.class) {
            Block parent1 = blockDao.selectById(block.getParentBlockId());
            Block parent2 = blockDao.selectById(parent1.getParentBlockId());
            Block parent3 = blockDao.selectById(parent2.getParentBlockId());
            localStatus.setMaxBlockHeight(block.getHeight());
            localStatus.setPreparedBlock(block);
            localStatus.setLockedBlock(parent2);
            localStatus.setCommittedBlock(parent3);
        }
    }

    @Override
    public Block getParentBlock(Block block) {
        return blockDao.selectById(block.getParentBlockId());
    }

    @Override
    public Boolean isSafeNewBlock(Block block) {
        try {
            // 安全规则1
            if (!(block.getHeight() > localStatus.getMaxBlockHeight())) {
                return false;
            }
            // 安全规则2
            Block parent = blockDao.selectById(block.getParentBlockId());
            if (parent == null) {
                return false;
            }
            // /判断𝑏𝑙𝑜𝑐𝑘的父区块𝑏𝑙𝑜𝑐𝑘Father.h𝑒𝑖𝑔h𝑡 > 𝑐𝑢𝑟𝐿𝑜𝑐𝑘𝑒𝑑𝐵𝑙𝑜𝑐𝑘.h𝑒𝑖𝑔h𝑡?
            if (parent.getHeight() > localStatus.getLockedBlock().getHeight()) {
                return true;
            }
            Block gParent = blockDao.selectById(parent.getParentBlockId());
            return gParent != null && gParent.getParentBlockId().equals(localStatus.getLockedBlock().getBlockId());
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }

    }
}
