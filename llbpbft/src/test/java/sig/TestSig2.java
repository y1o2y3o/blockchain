package sig;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public class TestSig2 {
    public static void main(String[] args) {
        // 门限(t,n)
        int t, n;
        t = 3;
        n = 4;

        // 初始化
        Pairing bp = PairingFactory.getPairing("a.properties");
        Field G1 = bp.getG1();
        Field Zr = bp.getZr();
        Element g = G1.newRandomElement();


        // 主私钥/公钥
        Element x = Zr.newRandomElement(); // 系统主私钥
        Element v = g.duplicate().powZn(x); // 系统主公钥


        BigInteger secret = x.toBigInteger(); // 秘密值=x
        BigInteger prime = new BigInteger(secret.bitLength() + 1, 256, new Random()).abs(); // 大素数p

        // 生成多项式
        BigInteger[] coeff = new BigInteger[t];
        coeff[0] = secret;
        for (int i = 1; i < t; i++) {
            BigInteger r;
            r = new BigInteger(prime.bitLength(), new Random()).abs().mod(prime);
            coeff[i] = r;
        }

        // 计算xi[] vi[]
        List<BigInteger> X = new ArrayList<>();
        List<Element> V = new ArrayList<>();
        for (int i = 1; i <= n; ++i) {
            // 计算分私钥xi = P(i)
            BigInteger xi = secret;
            for (int exp = 1; exp < t; exp++) {
                xi = xi.add(coeff[exp].multiply(BigInteger.valueOf(i).pow(exp)));
            }
            X.add(xi);
            // 计算分公钥vi
            Element vi = g.pow(xi).getImmutable();
            V.add(vi);
        }

        List<Element> partialSigs= new ArrayList<>();
        // 0号用户签名 signing
        String m = "message";
        byte[] m_hash = Integer.toString(m.hashCode()).getBytes();
        Element h = G1.newElementFromHash(m_hash, 0, m_hash.length);//h(M)
        Element partialSig = h.pow(X.get(0)); // 部分签名

        // 广播 h,partialSig
        // 部分签名验证
        Element pairing1 = bp.pairing(partialSig, g);
        Element pairing2 = bp.pairing(h, V.get(0));

        // 聚合签名
        Element aggrSig = G1.newElement(1);
        for (int i = 0; i < t; ++i) {
            BigInteger lam;
            BigInteger ans1 = BigInteger.ONE;
            BigInteger ans2 = BigInteger.ONE;
            for (int j = 1; j <= t; ++j) {
                if (j == i) continue;
                ans1 = ans1.multiply(BigInteger.valueOf(0 - j));
                ans2 = ans2.multiply(BigInteger.valueOf(i - j));
            }
            lam = ans1.divide(ans2).mod(prime);
            Element ans = partialSigs.get(i).duplicate().pow(lam);
            aggrSig = aggrSig.duplicate().mul(ans);
        }
//        aggrSig;
        Element sig = h.duplicate().powZn(x);
        new BigInteger(1, 2, new Random());
        BigInteger b;

        // verify
        Element pl = bp.pairing(g, sig);
//        Element pr = bp.pairing(h, g_x);
//        System.out.println(pr.isEqual(pl));
    }
}

class Pki<R, T> {

    //Map<ClientID,privkey>
    public static Map<Integer, BigInteger> privKeys = new HashMap<>();//分私钥（PKI保存）
    //Map<clientID,pubKey>
    public static Map<Integer, Element> pubKeys = new HashMap<>();//分公钥（需要公布）
    //Map<clientId,keyInfo>
    public static Map<Integer, KeyInfo> keyInfos = new HashMap<>();

    //初始化 - 一旦生成，就不可变！
    public static final Pairing bp = PairingFactory.getPairing("a.properties");
    public static final Field G1 = bp.getG1();
    public static final Field Zr = bp.getZr();
    public static final Element g = G1.newRandomElement().getImmutable();
    public static final int CERTAINTY = 256;
    public static final SecureRandom random = new SecureRandom();


    //(Element)主公钥/主私钥
    //(BigInteger)主私钥（Element）转换称为（BigInteger）形式，作为（secret）进行分割
    private static final Element privateMaster = Zr.newRandomElement();//主私钥（私有）
    public static final Element publicMaster = g.duplicate().powZn(privateMaster);//主公钥（目前没有什么作用）
    private static final BigInteger secret = privateMaster.toBigInteger();
    public static final BigInteger prime = new BigInteger(secret.bitLength() + 1, CERTAINTY, random);

    public static void split(final BigInteger secret, int needed, int available, BigInteger prime, Random random) {
        /*
         * split(final BigInteger secret, int needed, int available, BigInteger prime, Random random)
         *  - secret - 待分割的秘密
         *  - needed - 几份可拼凑成完整的
         *  - available - 分成几份
         *  - prime - 大素数
         *  - random - 随机数
         *
         * 此函数执行后，会将：privKeys,pubKeys填满
         * */


    }
}


class KeyInfo {

    private Element publicKey;
    private BigInteger privKey;

    public KeyInfo() {
    }

    public KeyInfo(Element publicKey, BigInteger privKey) {
        this.publicKey = publicKey;
        this.privKey = privKey;
    }

    @Override
    public String toString() {
        return "KeyInfo{" +
                "publicKey=" + publicKey +
                ", privKey=" + privKey +
                '}';
    }

    public Element getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(Element publicKey) {
        this.publicKey = publicKey;
    }

    public BigInteger getPrivKey() {
        return this.privKey;
    }

    public void setPrivKey(BigInteger privKey) {
        this.privKey = privKey;
    }
}





