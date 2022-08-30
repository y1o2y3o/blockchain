package com.bsp.exceptions;


import com.csp.web.ResultStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * CommonException
 *
 * @author 3
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommonException extends RuntimeException {
    private ResultStatus resultStatus;

    public CommonException(ResultStatus resultStatus) {
        super(resultStatus.getMsg());
        this.resultStatus = resultStatus;
    }

    public CommonException(String msg) {
        super(msg);
    }
}
