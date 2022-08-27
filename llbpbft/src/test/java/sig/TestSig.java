package sig;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Scanner;

public class TestSig {
    public static void main(String[] args) {
        System.out.println("*************************密钥生成*************************");
        Pairing pairing = PairingFactory.getPairing("a.properties");//由工厂类生成Pairing对象
        Field G1 = pairing.getG1();//Pairing对象获取G1循环加法群
        Field Zr = pairing.getZr();//Pairing对象获取Zr群，用于随机选取私钥
        Element g = G1.newRandomElement();//G1随机生成G1的生成元

        Element x = Zr.newRandomElement();//Zr群中随机选取一个元素作为生成元x
        Element g_x = g.duplicate().powZn(x);//公钥
        System.out.println("G1="+G1);
//        System.out.println(G1.getOrder());
//        System.out.println(Zr.getOrder());
        System.out.println("Zr="+Zr);
        System.out.println("g="+g);
        System.out.println("私钥"+x);
        System.out.println("公钥g_x="+g_x);
        System.out.println("***************************签名***************************");
        System.out.println("请输入要加密的消息：");
        Scanner scanner = new Scanner(System.in);
        String m = scanner.next();
        //String m="message";//明文
        byte[] bytes = Integer.toString(m.hashCode()).getBytes();//哈希明文，将整数转为字符串进而调用字符串获取字节数组的方法
        Element h = G1.newElementFromHash(bytes, 0, bytes.length);//将明文m映射为G1群中的元素
        Element s = h.duplicate().powZn(x);//x对m的签名
        Element e = G1.newElement();
        System.out.println("h="+h);
        System.out.println("s="+s);
        System.out.println("***************************验签***************************");
        Element pairing1 = pairing.pairing(g, s);
        Element pairing2 = pairing.pairing(h, g_x);
        System.out.println("pairing1="+pairing1);
        System.out.println("pairing2="+pairing2);
        if (pairing1.isEqual(pairing2)){
            System.out.println("验签成功");
        }else{
            throw new RuntimeException("验签失败！");
        }
        /*
        // Initialization
        Pairing bp = PairingFactory.getPairing("a.properties");
        Field G1 = bp.getG1();
        Field Zr = bp.getZr();
        Element g = G1.newRandomElement();
        Element x = Zr.newRandomElement();
        Element g_x = g.duplicate().powZn(x);
        System.out.println("G1="+G1);
        System.out.println("Zr="+Zr);
        System.out.println("g="+g);
        System.out.println("x="+x);
        System.out.println("g_x="+g_x);

        //Signing
        String m = "message";
        byte[] m_hash = Integer.toString(m.hashCode()).getBytes();
        Element h = G1.newElementFromHash(m_hash, 0, m_hash.length);
        Element sig = h.duplicate().powZn(x);
        System.out.println("h="+h);
        System.out.println("sig="+sig);

        //Verification
        Element pl = bp.pairing(g, sig);
        Element pr = bp.pairing(h, g_x);
        System.out.println("pl="+pl);
        System.out.println("pr="+pr);
        if (pl.isEqual(pr))
            System.out.println("Yes");
        else
            System.out.println("No");
         */
    }
}

