package com.bsp.web;


import com.bsp.exceptions.CommonException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


/**
 * 后端返回给前端的格式
 *
 * @author zks
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    /**
     * 业务错误码
     */
    private Integer code;
    /**
     * 信息描述
     */
    private String desc;
    /**
     * 返回参数
     */
    private T data;

    public Result(int code, String desc, T data) {
        this.code = code;
        this.desc = desc;
        this.data = data;
    }

    /**
     * 业务成功返回业务代码和描述信息
     */
    public static <T> Result<T> success() {
        return new Result<>(HttpStatus.OK.value(), null, null);
    }

    /**
     * 业务失败返回业务代码和描述信息
     */
    public static <T> Result<T> failure(String msg) {
        return new Result<>(ResultStatus.COMMON_EXCEPTION.getCode(), msg, null);
    }

    /**
     * 业务失败返回业务代码和描述信息
     */
    public static <T> Result<T> failure(ResultStatus resultStatus) {
        return new Result<>(resultStatus.getCode(), resultStatus.getMsg(), null);
    }

    /**
     * 业务失败
     */
    public static <T> Result<T> failure() {
        return failure("业务失败");
    }
}