package com.bsp.signatures.impl;

import com.alibaba.fastjson.JSON;
import com.csp.sig.SecretKeys;
import com.bsp.signatures.Signature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zksfromusa
 */
@Service
public class SignatureImpl implements Signature {
    @Autowired
    private LocalStatus localStatus;

    @Autowired
    private GlobalStatus globalStatus;


    public static void main(String[] args) {
        Pairing bp = PairingFactory.getPairing("a.properties");
        Field G1 = bp.getG1();
        Field Zr = bp.getZr();
        Element g = G1.newRandomElement();
        Element x = Zr.newRandomElement(); // 私钥
        Element g_x = g.duplicate().powZn(x); // 公钥

        // signing
        String m = "message";
        byte[] m_hash = Integer.toString(m.hashCode()).getBytes();
        Element h = G1.newElementFromHash(m_hash, 0, m_hash.length);
        Element sig = h.duplicate().powZn(x);

        // verify
        Element pl = bp.pairing(g, sig);
        Element pr = bp.pairing(h, g_x);
        System.out.println(pr.isEqual(pl));
    }

    @Override
    public String sign(Object block) {
        SecretKeys secretKeys = globalStatus.getSecretKeys();

        // 哈希明文
        byte[] bytes = JSON.toJSONString(block).getBytes();
        // 明文映射为G1群中的元素
        Element h = secretKeys.getG1().newElementFromHash(bytes, 0, bytes.length);
        // 签名
        Element sig = h.duplicate().powZn(secretKeys.getSK());
        return Base64.encodeBase64String(sig.toBytes());
    }

    @Override
    public Boolean validate(Object block, String sig) {
        SecretKeys secretKeys = globalStatus.getSecretKeys();
        Pairing pairing = secretKeys.getBp();
        // 哈希明文
        byte[] bytes = JSON.toJSONString(block).getBytes();
        // 明文映射为G1群中的元素
        Element h = secretKeys.getG1().newElementFromHash(bytes, 0, bytes.length);
        // 验证签名
        Element pairing1 = pairing.pairing(secretKeys.getG(), secretKeys.getG1().newElementFromBytes(Base64.decodeBase64(sig)));
        Element pairing2 = pairing.pairing(h, secretKeys.getPK());
        return pairing1.isEqual(pairing2);
    }


    public Boolean test(Object block) {
        SecretKeys secretKeys = globalStatus.getSecretKeys();
        // 哈希明文
        byte[] bytes = JSON.toJSONString(block).getBytes();
        // 明文映射为G1群中的元素
        Element h = secretKeys.getG1().newElementFromHash(bytes, 0, bytes.length);
        // 签名
        Element sig = h.duplicate().powZn(secretKeys.getSK());

        Pairing pairing = secretKeys.getBp();
        // 哈希明文
        bytes = JSON.toJSONString(block).getBytes();
        // 明文映射为G1群中的元素

        // 验证签名
        Element pairing1 = pairing.pairing(secretKeys.getG(), secretKeys.getG1().newElementFromBytes(sig.toBytes()));
        Element pairing2 = pairing.pairing(h, secretKeys.getPK());
        return pairing1.isEqual(pairing2);
    }
}
