package com.bsp.entity;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 密钥
 *
 * @author zksfromusa
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecretKeys {
    // 固定写死
    private Pairing bp;
    private Field G1;
    private Field Zr;

    // 需要从密钥中心同步
    private Element g;
    private Element partialSK; // 部分签名私钥
    private Element partialPK; // 部分签名公钥
    private Element SK; // 系统主私钥
    private Element PK; // 系统主公钥
    private BigInteger prime; // 大素数p

}
