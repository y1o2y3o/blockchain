package com.bsp.signatures;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.List;
import java.util.Set;

/**
 * 门限签名
 *
 * @author zksfromusa
 */
public interface ThresholdSignature {
    /**
     * 部分签名
     * @param block
     * @return 部分签名partialSig
     */
    String partialSign(Object block);

    /**
     * 部分签名验证
     * @param block
     * @param partialSig
     * @return
     */
    Boolean partialValidate(Object block, String partialSig, String url);

    /**
     * 聚合签名
     * @param block
     * @param partialSigs 部分签名的集合
     * @return
     */
    String aggrSign(Object block, List<String> partialSigs);

    /**
     * 聚合签名验证
     * @param block
     * @param aggrSig
     * @return
     */
    Boolean aggrValidata(Object block, String aggrSig);
}
