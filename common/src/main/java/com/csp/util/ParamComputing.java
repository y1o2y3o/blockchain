package com.csp.util;

public class ParamComputing {
    /**
     * 容错
     *
     * @param n 总结点数
     * @return
     */
    public static int getF(int n) {
        int f = n / 3 - (n % 3 == 0 ? 1 : 0);
        return f;
    }
}
