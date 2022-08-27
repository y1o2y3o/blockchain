package com.bsp.enums;

import lombok.AllArgsConstructor;

/**
 * @author zksfromusa
 */

@AllArgsConstructor
public enum FlagEnum {
    COMMITTED("COMMITTED"),
    LOCKED("LOCKED"),
    PREPARED("PREPARED");

    String desc;


    @Override
    public String toString() {
        return desc;
    }
}
