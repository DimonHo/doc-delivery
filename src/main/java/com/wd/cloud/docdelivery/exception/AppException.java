package com.wd.cloud.docdelivery.exception;

import com.wd.cloud.commons.enums.StatusEnum;
import com.wd.cloud.commons.exception.ApiException;

/**
 * @author He Zhigang
 * @date 2019/1/21
 * @Description:
 */
public class AppException extends ApiException {

    public AppException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.status(), exceptionEnum.message());
    }

    public AppException(StatusEnum statusEnum) {
        super(statusEnum.value(), statusEnum.message());
    }

    public AppException(Integer status, String message) {
        super(status, message);
    }
}
