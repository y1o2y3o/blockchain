package com.csp.web;

import org.springframework.http.HttpStatus;

/**
 * @author zzkkss
 */

public enum ResultStatus {

    /**
     * OK
     */
    SUCCESS_HTTP(HttpStatus.OK, 200, "OK"),
    /**
     * Bad Request
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "Bad Request"),
    /**
     * Internal Server Error
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal Server Error"),


    /**
     * 客户端错误
     */
    COMMON_EXCEPTION(-1),

    /**
     * 正在同步区块高度
     */
    SYNC_PROCESS(-1,"正在同步区块高度"),

    /**
     * 区块插入错误
     */
    BLOCK_INSERT_ERROR(-1, "区块插入错误"),

    /**
     * 成功
     */
    SUCCESS(0, "成功"),

    /**
     * 成功
     */
    REDIRECT(0, "正在转发给领导"),

    /**
     * 试图切换
     */
    CHANGE_VIEW(-5, "验证不通过，视图切换"),
    /**
     * 不是领导
     */
    NOT_LEADER(-2, "不是领导"),
    /**
     * 不是副本
     */
    NOT_REPLICA(-2, "不是副本"),
    /**
     * 领导尚未收集到n-f个投票
     */
    NOT_READY(-3, "领导尚未收集到n-f个投票"),
    /**
     * 实体已经存在
     */
    HTTP_200_OK(200, "OK"),

    /**
     * 更新异常
     */
    UPDATE_ERROR(-3, "更新异常"),
    /**
     * 查询异常
     */
    QUERY_ERROR(-4, "查询异常"),
    /**
     * 远程文件不存在
     */
    REMOTE_FILE_NOT_EXIST(-5, "远程文件不存在");

    /**
     * 返回的HTTP状态码,  符合http请求
     */
    private HttpStatus httpStatus;
    /**
     * 业务异常码
     */
    private Integer code;
    /**
     * 业务异常信息描述
     */
    private String msg;

    ResultStatus(Integer code) {
        this.code = code;
    }

    ResultStatus(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    ResultStatus(HttpStatus httpStatus, Integer code, String msg) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.msg = msg;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}