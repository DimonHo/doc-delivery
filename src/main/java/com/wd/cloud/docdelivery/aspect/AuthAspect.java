package com.wd.cloud.docdelivery.aspect;

import com.wd.cloud.commons.exception.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: He Zhigang
 * @Date: 2019/3/23 9:41
 * @Description: 登陆验证切面
 */
@Slf4j
@Aspect
@Component
@Order(8)
public class AuthAspect {
    @Autowired
    HttpServletRequest request;

    @Before(value = "@annotation(com.wd.cloud.commons.annotation.ValidateLogin)")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        Assertion principal = (Assertion) request.getSession().getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
        if (principal == null) {
            throw new AuthException();
        }
    }
}
