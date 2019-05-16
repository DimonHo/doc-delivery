package com.wd.cloud.docdelivery.exception;

import com.wd.cloud.commons.exception.ApiException;

/**
 * @Author: He Zhigang
 * @Date: 2019/3/28 15:22
 * @Description:
 */
public class RepeatHelpRequestException extends ApiException {

    public RepeatHelpRequestException() {
        super(ExceptionEnum.HELP_REPEAT.status(), ExceptionEnum.HELP_REPEAT.message());
    }
}
