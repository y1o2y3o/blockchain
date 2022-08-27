package com.bsp.signatures.impl;

import com.csp.sig.SecretKeys;
import com.bsp.signatures.ThresholdSignature;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.bsp.utils.Hashing;
import it.unisa.dia.gas.jpbc.Element;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

/**
 * 门限签名
 *
 * @author zksfromusa
 */
@Service
public class ThresholdSignatureImpl implements ThresholdSignature {

    @Autowired
    private LocalStatus localStatus;

    @Autowired
    private GlobalStatus globalStatus;

    @Override
    public String partialSign(Object block) {
        SecretKeys secretKeys = globalStatus.getSecretKeys();
        // 哈希明文
        byte[] bytes = Hashing.toBytes(block);
        // 明文映射为G1群中的元素
        Element h = secretKeys.getG1().newElementFromHash(bytes, 0, bytes.length);

        Element partialSig = h.duplicate().powZn(secretKeys.getPartialSK()); // 部分签名

        return Base64.encodeBase64String(partialSig.toBytes());
    }

    @Override
    public Boolean partialValidate(Object block, String partialSig, String url) {
        SecretKeys secretKeys = globalStatus.getSecretKeys(); // 本节点keys
        SecretKeys targetSecretKeys = globalStatus.getHostSecretKeyMap().get(url); // 来自url节点的keys
        // 哈希明文
        byte[] bytes = Hashing.toBytes(block);
        // 明文映射为G1群中的元素
        Element h = secretKeys.getG1().newElementFromHash(bytes, 0, bytes.length);
        // e(partialSig_i, g)=e(h(M), partialPK_i)?
        Element pairing1 = secretKeys.getBp().pairing(secretKeys.getG1().newElementFromBytes(Base64.decodeBase64(partialSig)), secretKeys.getG().duplicate());
        Element pairing2 = secretKeys.getBp().pairing(h.duplicate(), targetSecretKeys.getPartialPK().duplicate()); // url节点的公钥验证
        return pairing1.isEqual(pairing2);
    }

    @Override
    public String aggrSign(Object block, List<String> partialSigs) {
        SecretKeys secretKeys = globalStatus.getSecretKeys(); // 本节点keys

        // 聚合签名
        Element aggrSig = secretKeys.getG1().newOneElement();
        int t = partialSigs.size();
        for (int i = 1; i <= t; ++i) {
            BigInteger ans1 = BigInteger.ONE;
            BigInteger ans2 = BigInteger.ONE;
            for (int j = 1; j <= t; ++j) {
                if (j == i) {
                    continue;
                }
                ans1 = ans1.multiply(BigInteger.valueOf(0 - j));
                ans2 = ans2.multiply(BigInteger.valueOf(i - j));
            }
            Element lam = secretKeys.getZr().newElement(ans1.divide(ans2));
            Element ans = secretKeys.getG1().newElementFromBytes(Base64.decodeBase64(partialSigs.get(i - 1))).powZn(lam);
            aggrSig = aggrSig.duplicate().mul(ans);
        }
        return Base64.encodeBase64String(aggrSig.toBytes());
    }

    @Override
    public Boolean aggrValidata(Object block, String aggrSig) {
        SecretKeys secretKeys = globalStatus.getSecretKeys(); // 本节点keys
        // 哈希明文
        byte[] bytes = Hashing.toBytes(block);
        // 明文映射为G1群中的元素
        Element h = secretKeys.getG1().newElementFromHash(bytes, 0, bytes.length);
        // e(aggrSig,g)?=e(h(M),PK)?
        Element pairing1 = secretKeys.getBp().pairing(secretKeys.getG1().newElementFromBytes(Base64.decodeBase64(aggrSig)), secretKeys.getG().duplicate());
        Element pairing2 = secretKeys.getBp().pairing(h.duplicate(), secretKeys.getPK().duplicate()); // 本节点的公钥验证
        return pairing1.isEqual(pairing2);
    }


//    // 门限(t, n)
//    static int n = 9;
//    static int t = 4;
//    // 设置secretKeys
//    // 需要从密钥中心同步
//    static Pairing bp = PairingFactory.getPairing("a.properties");
//    static Field G1 = bp.getG1();
//    static Field Zr = bp.getZr();
//    static Element g = G1.newRandomElement();
//    static Element partialSK = null; // 部分签名私钥
//    static Element partialPK = null; // 部分签名公钥
//    static Element SK = Zr.newRandomElement(); // 系统主私钥
//    static Element PK = g.duplicate().powZn(SK); // 系统主公钥
//    static Element[] coeff;
//
//    static {
//        // 生成多项式
//        coeff = new Element[t];
//        coeff[0] = SK.duplicate();
//        for (int j = 1; j < t; j++) {
//            coeff[j] = Zr.newRandomElement();
//        }
//    }
//
//    public static void main(String[] args) {
//
//
//        // 计算分私钥xi[] 分公钥vi[]
//        List<Element> X = new ArrayList<>();
//        List<Element> V = new ArrayList<>();
//        for (int i = 1; i <= n; ++i) {
//            // 计算分私钥xi = P(i)
//            Element xi = P(i).duplicate();
//            X.add(xi);
//            // 计算分公钥vi
//            Element vi = g.duplicate().powZn(xi);
//            V.add(vi);
//        }
//
//
//        // 初始化
//        List<Element> pSigs = new ArrayList<>(); // 部分签名集合
//        byte[] bytes = "block".getBytes(); // 明文M
//        Element h = G1.newElementFromHash(bytes, 0, bytes.length).getImmutable(); // h(M)
//
//        // 部分签名
//        for (int i = 0; i < n; ++i) {
//            Element partialSig = h.duplicate().powZn(X.get(i)).getImmutable(); // partialSig签名
//            Element pairing1 = bp.pairing(partialSig.duplicate().getImmutable(), g.duplicate().getImmutable());
//            Element pairing2 = bp.pairing(h.duplicate().getImmutable(), V.get(i).duplicate().getImmutable()); // url节点的公钥验证
//            if (!pairing1.isEqual(pairing2)) {
//                System.out.println("部分验证不通过！");
//            } else {
//                if (pSigs.size() < t) {
//                    pSigs.add(partialSig);
//                }
//            }
//        }
//
//
//        // 聚合签名
//        Element aggrSig = G1.newOneElement().getImmutable(); // 聚合签名初始化
//        for (int i = 1; i <= pSigs.size(); ++i) {
//            BigInteger ans1 = BigInteger.ONE;
//            BigInteger ans2 = BigInteger.ONE;
//            for (int j = 1; j <= pSigs.size(); ++j) {
//                if (j == i) {
//                    continue;
//                }
//                ans1 = ans1.multiply(BigInteger.valueOf(0 - j));
//                ans2 = ans2.multiply(BigInteger.valueOf(i - j));
//            }
////            System.out.println(ans1+","+ans2);
//            Element lam = Zr.newElement(ans1.divide(ans2)).getImmutable();
////            System.out.println(lam);
//            Element ans = pSigs.get(i - 1).duplicate().powZn(lam);
//            aggrSig = aggrSig.duplicate().mul(ans);
//        }
//        // 聚合签名验证
//        Element pairing1 = bp.pairing(aggrSig.duplicate(), g.duplicate());
//        Element pairing2 = bp.pairing(h.duplicate(), PK.duplicate()); // 本节点的公钥验证
//        System.out.println(pairing1.isEqual(pairing2));
//    }
//
//    // Z_p上的t-1多项式P(i)
//    private static Element P(int i) {
//        Element res = Zr.newZeroElement();
//        for (int j = 0; j < t; j++) {
//            res = res.add(coeff[j].duplicate().mul(BigInteger.valueOf(i).pow(j)));
//        }
//        return res;
//    }
}
