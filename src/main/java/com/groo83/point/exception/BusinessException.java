package com.groo83.point.exception;

import com.groo83.point.common.code.ErrorCode;

public class BusinessException extends RuntimeException {

    private ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }


    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
