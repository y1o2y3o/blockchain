package com.bsp.utils;

import com.alibaba.fastjson.JSON;
import com.bsp.exceptions.CommonException;
import org.apache.tomcat.util.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zksfromusa
 */
public class Hashing {

    /**
     * 转为字节数组(JSON)
     *
     * @param block
     * @return
     */
    public static byte[] toBytes(Object block) {
        // 哈希明文
        byte[] bytes = JSON.toJSONString(block).getBytes();
        return bytes;
    }

    /**
     * 产生哈希摘要
     *
     * @param block
     * @return
     */
    public static String genHashDigest(Object block) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA");
            sha.update(toBytes(block));
            return Base64.encodeBase64String(sha.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new CommonException("sha哈希转换执行异常");
        }
    }
}
