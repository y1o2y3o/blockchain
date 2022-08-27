import com.rsp.MainApplication;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = {MainApplication.class})
@RunWith(SpringRunner.class)
@Slf4j
public class TestPB {
    private Pairing bp;
    private Field G1;
    private Field Zr;
    private Element g;
    private Element partialSK = null; // 部分签名私钥
    private Element partialPK = null; // 部分签名公钥
    private Element SK; // 系统主私钥
    private Element PK; // 系统主公钥
    // 门限(t, n)
    private int n = 9;
    private int t = 4;
    Element[] coeff;

    @Test
    public void test00() {

        // 设置secretKeys
        // 需要从密钥中心同步
        bp = PairingFactory.getPairing("a.properties");
        G1 = bp.getG1();
        Zr = bp.getZr();

        g = G1.newElement(22222);
        partialSK = null; // 部分签名私钥
        partialPK = null; // 部分签名公钥
        SK = Zr.newElement(33333); // 系统主私钥
        PK = g.duplicate().powZn(SK); // 系统主公钥
        System.out.println(SK.toBytes());
    }

    @Test
    public void test01() {

        // 设置secretKeys
        // 需要从密钥中心同步
        bp = PairingFactory.getPairing("a.properties");
        G1 = bp.getG1();
        Zr = bp.getZr();
        g = G1.newRandomElement();
        partialSK = null; // 部分签名私钥
        partialPK = null; // 部分签名公钥
        SK = Zr.newRandomElement(); // 系统主私钥
        PK = g.duplicate().powZn(SK); // 系统主公钥
        System.out.println(SK);

        // 生成多项式
        coeff = new Element[t];
        coeff[0] = SK.duplicate();
        for (int j = 1; j < t; j++) {
            coeff[j] = Zr.newRandomElement();

        }

        // 计算分私钥xi[] 分公钥vi[]
        List<Element> X = new ArrayList<>();
        List<Element> V = new ArrayList<>();
        for (int i = 1; i <= n; ++i) {
            // 计算分私钥xi = P(i)
            Element xi = P(i).duplicate();
            X.add(xi);
            // 计算分公钥vi
            Element vi = g.duplicate().powZn(xi);
            V.add(vi);
        }


        // 初始化
        List<Element> pSigs = new ArrayList<>(); // 部分签名集合
        byte[] bytes = "block".getBytes(); // 明文M
        Element h = G1.newElementFromHash(bytes, 0, bytes.length).getImmutable(); // h(M)

        // 部分签名
        for (int i = 0; i < n; ++i) {
            Element partialSig = h.duplicate().powZn(X.get(i)).getImmutable(); // partialSig签名
            Element pairing1 = bp.pairing(partialSig.duplicate().getImmutable(), g.duplicate().getImmutable());
            Element pairing2 = bp.pairing(h.duplicate().getImmutable(), V.get(i).duplicate().getImmutable()); // url节点的公钥验证
            if (!pairing1.isEqual(pairing2)) {
                System.out.println("部分验证不通过！");
            } else {
                if (pSigs.size() < t) {
                    pSigs.add(partialSig);
                }
            }
        }


        // 聚合签名
        Element aggrSig = G1.newOneElement().getImmutable(); // 聚合签名初始化
        for (int i = 1; i <= pSigs.size(); ++i) {
            BigInteger ans1 = BigInteger.ONE;
            BigInteger ans2 = BigInteger.ONE;
            for (int j = 1; j <= pSigs.size(); ++j) {
                if (j == i) {
                    continue;
                }
                ans1 = ans1.multiply(BigInteger.valueOf(0 - j));
                ans2 = ans2.multiply(BigInteger.valueOf(i - j));
            }
//            System.out.println(ans1+","+ans2);
            Element lam = Zr.newElement(ans1.divide(ans2)).getImmutable();
//            System.out.println(lam);
            Element ans = pSigs.get(i - 1).duplicate().powZn(lam);
            aggrSig = aggrSig.duplicate().mul(ans);
        }
        // 聚合签名验证
        Element pairing1 = bp.pairing(aggrSig.duplicate(), g.duplicate());
        Element pairing2 = bp.pairing(h.duplicate(), PK.duplicate()); // 本节点的公钥验证
        System.out.println(pairing1.isEqual(pairing2));


    }

    // Z_p上的t-1多项式P(i)
    private Element P(int i) {
        Element res = Zr.newZeroElement();
        for (int j = 0; j < t; j++) {
            res = res.add(coeff[j].duplicate().mul(BigInteger.valueOf(i).pow(j)));
        }
        return res;
    }
}
