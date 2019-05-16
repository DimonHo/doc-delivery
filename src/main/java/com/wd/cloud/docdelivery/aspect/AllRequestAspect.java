package com.wd.cloud.docdelivery.aspect;

import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wd.cloud.commons.constant.SessionConstant;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.commons.util.StrUtil;
import com.wd.cloud.docdelivery.feign.UoServerApi;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: He Zhigang
 * @Date: 2019/4/10 18:35
 * @Description: session 切面
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class AllRequestAspect {

    @Autowired
    HttpServletRequest request;

    @Autowired
    UoServerApi uoServerApi;

    @Pointcut("execution(public * com.wd.cloud.docdelivery.controller.*.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        Assertion principal = (Assertion) request.getSession().getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
        // 如果用户已登录
        if (principal != null) {
            String casUsername = principal.getPrincipal().getName();
            JSONObject sessionUser = JSONUtil.parseObj(request.getSession().getAttribute(SessionConstant.LOGIN_USER));
            String sessionUsername = sessionUser.isEmpty() ? null : sessionUser.getStr("username");
            JSONObject sessionOrg = JSONUtil.parseObj(request.getSession().getAttribute(SessionConstant.ORG));
            // session中已存在用户信息，则跳过
            if (casUsername.equals(sessionUsername) && !sessionOrg.isEmpty()) {
                return;
            }
            // 获取用户信息
            ResponseModel<JSONObject> userResponse = uoServerApi.user(casUsername);
            log.info("调用uo-server获取用户信息【{}】", userResponse);
            if (!userResponse.isError()) {
                sessionUser = userResponse.getBody();
                String orgFlag = sessionUser.getStr("orgFlag");
                String loginIp = sessionUser.getStr("lastLoginIp");
                // 证件照验证状态 2为已验证
                Integer validStatus = sessionUser.getInt("validStatus");
                // 如果用户所属某个机构，则以该机构作为访问机构
                if (StrUtil.isNotBlank(orgFlag)) {
                    // 获取用户的机构信息
                    ResponseModel<JSONObject> orgFlagResponse = uoServerApi.org(null, orgFlag, null);
                    log.info("调用uo-server获取【{}】用户的机构信息【{}】", casUsername, orgFlagResponse);
                    if (!orgFlagResponse.isError()) {
                        String orgName = orgFlagResponse.getBody().getStr("name");
                        sessionOrg = new JSONObject();
                        sessionOrg.put("flag", orgFlag);
                        sessionOrg.put("name", orgName);
                        request.getSession().setAttribute(SessionConstant.ORG, sessionOrg);
                    }
                }
                // 获取最后用户最后登陆IP的机构信息
                ResponseModel<JSONObject> orgIpResponse = uoServerApi.org(null, null, loginIp);
                log.info("调用uo-server获取用户【{}】登陆IP【{}】的机构信息【{}】", casUsername, loginIp, orgIpResponse);
                // 用户最后登陆IP未找到对应机构信息，表示校外登陆
                if (orgIpResponse.isError() && orgIpResponse.getStatus() == 404) {
                    request.getSession().setAttribute(SessionConstant.IS_OUT, true);
                    request.getSession().setAttribute(SessionConstant.LEVEL, validStatus == 2 ? 6 : 2);
                } else {
                    request.getSession().setAttribute(SessionConstant.IS_OUT, false);
                    request.getSession().setAttribute(SessionConstant.LEVEL, validStatus == 2 ? 7 : 3);
                }
                request.getSession().setAttribute(SessionConstant.LOGIN_USER, sessionUser);
            }

        } else {
            // 如果sso退出登陆，清空session中的用户信息
            cleanSession(request);
            JSONObject sessionOrg = JSONUtil.parseObj(request.getSession().getAttribute(SessionConstant.ORG));
            Boolean isOut = (Boolean) request.getSession().getAttribute(SessionConstant.IS_OUT);
            Integer level = (Integer) request.getSession().getAttribute(SessionConstant.LEVEL);
            if (sessionOrg.isEmpty() || isOut == null || level == null) {
                String clientIp = ServletUtil.getClientIP(request);
                log.info("客户端访问IP = {}", clientIp);
                ResponseModel<JSONObject> orgResponse = uoServerApi.org(null, null, clientIp);
                if (orgResponse.isError()) {
                    //校外访问
                    request.getSession().setAttribute(SessionConstant.IS_OUT, true);
                    request.getSession().setAttribute(SessionConstant.LEVEL, 0);
                } else {
                    //非校外访问
                    request.getSession().setAttribute(SessionConstant.IS_OUT, false);
                    request.getSession().setAttribute(SessionConstant.LEVEL, 1);
                    request.getSession().setAttribute(SessionConstant.ORG, orgResponse.getBody());
                }
            }
        }
    }

    private void cleanSession(HttpServletRequest request) {
        if (request.getSession().getAttribute(SessionConstant.LOGIN_USER) != null) {
            request.getSession().removeAttribute(SessionConstant.LOGIN_USER);
            request.getSession().removeAttribute(SessionConstant.ORG);
            request.getSession().removeAttribute(SessionConstant.IS_OUT);
            request.getSession().removeAttribute(SessionConstant.LEVEL);
        }
    }

}
