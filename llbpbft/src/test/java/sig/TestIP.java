package sig;

import com.bsp.MainApplication;
import com.bsp.conf.ServerConfig;
import com.bsp.entity.Block;
import com.bsp.entity.SecretKeys;
import com.bsp.signatures.ThresholdSignature;
import com.bsp.signatures.impl.SignatureImpl;
import com.bsp.status.GlobalStatus;
import com.bsp.status.LocalStatus;
import com.bsp.utils.ParamComputing;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;

@SpringBootTest(classes = {MainApplication.class})
@RunWith(SpringRunner.class)
public class TestIP {
    @Resource
    private ServerConfig serverConfig;

    @Resource
    private LocalStatus localStatus;

    @Resource
    private SignatureImpl signature;

    @Resource
    private ThresholdSignature thresholdSignature;
    @Resource
    private GlobalStatus globalStatus;

    private int n;
    private int t;

    @Test
    public void test1() {
//        System.out.println(localStatus);
        System.out.println(serverConfig.getUrl());
    }

    @Test
    public void test2() {
        Block b = new Block();
        System.out.println(signature.validate(b, signature.sign(b)));
//        System.out.println(signature.sign(new Block()));
//        System.out.println(signature.test(b));
    }

    @Test
    public void test3() {
        Block b = new Block();
//        System.out.println(signature.validate(b, signature.sign(b)));
        SecretKeys secretKeys = globalStatus.getSecretKeys();
        Map<String, SecretKeys> hostSecretKeyMap = globalStatus.getHostSecretKeyMap();
        // 初始化
        // 模拟用户部分签名
        List<String> partialSigList = new ArrayList<>();
        for (String host : globalStatus.getHostList()) {
            secretKeys.setPartialSK(hostSecretKeyMap.get(host).getPartialSK());
            secretKeys.setPartialPK(hostSecretKeyMap.get(host).getPartialPK());
            String psig = thresholdSignature.partialSign(b);
            System.out.println("host=" + host + ",pSig=" + thresholdSignature.partialValidate(b, psig, host)); //验证部分签名
            if (partialSigList.size() <= t) { // 直到num=t
                partialSigList.add(psig);
            }
        }
        String aggrSig = thresholdSignature.aggrSign(b, partialSigList);
        System.out.println("aggrSig=" + thresholdSignature.aggrValidata(b, aggrSig));

//        System.out.println(signature.sign(new Block()));
//        System.out.println(signature.test(b));
    }

    // 生成密钥
    @Before
    public void init() {
        // 设置secretKeys
        // 需要从密钥中心同步
        Pairing bp = PairingFactory.getPairing("a.properties");
        Field G1 = bp.getG1();
        Field Zr = bp.getZr();
        Element g = G1.newRandomElement();
        Element partialSK = null; // 部分签名私钥
        Element partialPK = null; // 部分签名公钥
        Element SK = Zr.newRandomElement(); // 系统主私钥
        Element PK = g.duplicate().powZn(SK); // 系统主公钥

        // set secretKeys
        SecretKeys secretKeys = SecretKeys.builder().bp(bp).G1(G1).Zr(Zr).g(g).partialSK(partialSK).partialPK(partialPK).SK(SK).PK(PK).build();
        globalStatus.setSecretKeys(secretKeys);

        // 初始化hostslist
        globalStatus.setHostList(new ArrayList<String>() {
            {
                this.add("1");
                this.add("2");
                this.add("3");
                this.add("4");
                this.add("5");
            }
        });
        List<String> hostList = globalStatus.getHostList();

        // 门限(t, n)
        n = hostList.size();
        t = n - ParamComputing.getF(n);

        // 生成多项式
        Element[] coeff = new Element[t];
        coeff[0] = SK.duplicate();
        for (int i = 1; i < t; i++) {
            coeff[i] = Zr.newRandomElement();
        }

        // 计算分私钥xi[] 分公钥vi[]
        Map<String, SecretKeys> hostSecretKeyMap = globalStatus.getHostSecretKeyMap();
        List<Element> X = new ArrayList<>();
        List<Element> V = new ArrayList<>();
        for (int i = 1; i <= n; ++i) {
            // 计算分私钥xi = P(i)
            Element xi = Zr.newZeroElement();
            for (int j = 0; j < t; j++) {
                xi = xi.add(coeff[j].duplicate().mul(BigInteger.valueOf(i).pow(j)));
            }
            X.add(xi);
            // 计算分公钥vi
            Element vi = g.duplicate().powZn(xi);
            V.add(vi);
        }

        // 设置hostSecretKeyMap
        for (int i = 0; i < hostList.size(); ++i) {
            String host = hostList.get(i);
            hostSecretKeyMap.put(host, SecretKeys.builder().partialSK(X.get(i)).partialPK(V.get(i)).build());
        }
    }

//    @Before
//    public void init() {
//        // 设置secretKeys
//        // 需要从密钥中心同步
//        Pairing bp = PairingFactory.getPairing("a.properties");
//        Field G1 = bp.getG1();
//        Field Zr = bp.getZr();
//        Element g = G1.newRandomElement();
//        BigInteger partialSK = null; // 部分签名私钥
//        Element partialPK = null; // 部分签名公钥
//        Element SK = Zr.newRandomElement(); // 系统主私钥
//        Element PK = g.duplicate().powZn(SK); // 系统主公钥
//        BigInteger secret = SK.toBigInteger();
//        BigInteger prime = new BigInteger(secret.bitLength() + 1, 256, new Random()).abs(); // 大素数p
//
//        // set secretKeys
//        SecretKeys secretKeys = SecretKeys.builder().bp(bp).G1(G1).Zr(Zr).g(g).partialSK(partialSK).partialPK(partialPK).SK(SK).PK(PK).prime(prime).build();
//        globalStatus.setSecretKeys(secretKeys);
//
//        // 初始化hostslist
//        globalStatus.setHostList(new ArrayList<String>() {
//            {
//                this.add("1");
//                this.add("2");
//                this.add("3");
//                this.add("4");
//            }
//        });
//        List<String> hostList = globalStatus.getHostList();
//
//        // 门限(t, n)
//        n = hostList.size();
//        t = n - ParamComputing.getF(n);
//
//        // 生成多项式
//        BigInteger[] coeff = new BigInteger[t];
//        coeff[0] = secret;
//        for (int i = 1; i < t; i++) {
//            BigInteger r;
//            r = new BigInteger(prime.bitLength(), new Random()).abs().mod(prime);
//            coeff[i] = r;
//        }
//
//        // 计算分私钥xi[] 分公钥vi[]
//        Map<String, SecretKeys> hostSecretKeyMap = globalStatus.getHostSecretKeyMap();
//        List<BigInteger> X = new ArrayList<>();
//        List<Element> V = new ArrayList<>();
//        for (int i = 1; i <= n; ++i) {
//            // 计算分私钥xi = P(i)
//            BigInteger xi = BigInteger.ZERO;
//            for (int exp = 0; exp < t; exp++) {
//                xi = xi.add(coeff[exp].multiply(BigInteger.valueOf(i).pow(exp)));
//            }
//            X.add(xi);
//            // 计算分公钥vi
//            Element vi = g.duplicate().pow(xi).getImmutable();
//            V.add(vi);
//        }
//
//        // 设置hostSecretKeyMap
//        for (int i = 0; i < hostList.size(); ++i) {
//            String host = hostList.get(i);
//            hostSecretKeyMap.put(host, SecretKeys.builder().partialSK(X.get(i)).partialPK(V.get(i)).build());
//        }
//    }

}

