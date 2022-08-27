package com.bsp.enums;

import lombok.AllArgsConstructor;

/**
 * @author zksfromusa
 */

@AllArgsConstructor
public enum MessageEnum {
    CHANGE_VIEW("CHANGE_VIEW"),
    SYNC_HEIGHT("SYNC_HEIGHT"),
    UPLOAD_BLOCKS("UPLOAD_BLOCKS"),
    SYNC_HEIGHT_VOTE("SYNC_HEIGHT_VOTE"),
    PROPOSAL("PROPOSAL"),
    PROPOSAL_VOTE("PROPOSAL_VOTE");

    String desc;


    @Override
    public String toString() {
        return desc;
    }
}
