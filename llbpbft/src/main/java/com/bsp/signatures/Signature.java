package com.bsp.signatures;

import com.bsp.entity.Block;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zksfromusa
 */
@Service
public interface Signature {
    /**
     * 签名
     *
     * @param block
     * @return
     */
    String sign(Object block);

    /**
     * 验证
     *
     * @param block
     * @param sig
     * @return
     */
    Boolean validate(Object block, String sig);
}
