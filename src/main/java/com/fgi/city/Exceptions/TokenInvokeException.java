package com.fgi.city.Exceptions;

/**
 * 自定义账号调用异常
 */
public class TokenInvokeException extends Exception {

    private String errorCode;
    private String errorMsg;

    public TokenInvokeException() {
    }

    public TokenInvokeException(String errorCode) {
        this.errorCode = errorCode;
    }

    public TokenInvokeException(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
