package com.bsp.signatures;

import lombok.Data;
import org.springframework.stereotype.Component;

// 负责加解密
@Data
@Component
public class EncodingBase {
    public String genHashCode(String msg){
        return "0";
    }
}
