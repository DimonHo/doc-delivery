package com.wd.cloud.docdelivery.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wd.cloud.commons.constant.SessionConstant;
import com.wd.cloud.commons.model.ResponseModel;
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
import javax.servlet.http.HttpSession;

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
        HttpSession session = request.getSession();
        Assertion principal = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
        // 如果用户已登录
        if (principal != null) {
            String casUsername = principal.getPrincipal().getName();
            JSONObject sessionUser = JSONUtil.parseObj(session.getAttribute(SessionConstant.LOGIN_USER));
            String sessionUsername = sessionUser.isEmpty() ? null : sessionUser.getStr("username");
            JSONObject sessionOrg = JSONUtil.parseObj(session.getAttribute(SessionConstant.ORG));
            // session中已存在用户信息，则跳过
            if (casUsername.equals(sessionUsername) && !sessionOrg.isEmpty()) {
                return;
            }
            buildLoginSession(session, casUsername);
        } else {
            // 如果sso退出登陆，清空session中的用户信息
            cleanSession(request);
            JSONObject sessionOrg = JSONUtil.parseObj(session.getAttribute(SessionConstant.ORG));
            Boolean isInside = (Boolean) session.getAttribute(SessionConstant.IS_INSIDE);
            if (sessionOrg.isEmpty() || isInside == null) {
                String clientIp = ServletUtil.getClientIP(request);
                buildNotLoginSession(session, clientIp);
            }
        }
    }

    /**
     * set登陆用户session
     * @param session
     * @param username
     */
    private void buildLoginSession(HttpSession session, String username) {
        // 获取用户信息
        ResponseModel<JSONObject> userResponse = uoServerApi.user(username);
        log.debug("调用uo-server获取用户信息【{}】", userResponse);
        if (!userResponse.isError()) {
            JSONObject sessionUser = userResponse.getBody();
            String orgFlag = sessionUser.getStr("orgFlag");
            String loginIp = sessionUser.getStr("lastLoginIp");
            // 如果用户所属某个机构，则以该机构作为访问机构
            if (StrUtil.isNotBlank(orgFlag)) {
                // 获取用户的机构信息
                ResponseModel<JSONObject> orgFlagResponse = uoServerApi.org(null, orgFlag, null);
                log.debug("调用uo-server获取【{}】用户的机构信息【{}】", username, orgFlagResponse);
                if (!orgFlagResponse.isError()) {
                    session.setAttribute(SessionConstant.ORG, orgFlagResponse.getBody());
                }
            }
            // 获取最后用户最后登陆IP的机构信息
            ResponseModel<JSONObject> orgIpResponse = uoServerApi.org(null, null, loginIp);
            log.debug("调用uo-server获取用户【{}】登陆IP【{}】的机构信息【{}】", username, loginIp, orgIpResponse);
            // 用户最后登陆IP未找到对应机构信息，表示校外登陆
            if (orgIpResponse.isError() || orgIpResponse.getStatus() == 404) {
                session.setAttribute(SessionConstant.IS_INSIDE, false);
            } else {
                session.setAttribute(SessionConstant.IS_INSIDE, true);
                session.setAttribute(SessionConstant.IP_ORG, orgIpResponse.getBody());
            }
            session.setAttribute(SessionConstant.LOGIN_USER, sessionUser);
        }
    }

    /**
     * set未登录用户session
     * @param session
     * @param clientIp
     */
    private void buildNotLoginSession(HttpSession session, String clientIp) {
        ResponseModel<JSONObject> orgResponse = uoServerApi.org(null, null, clientIp);
        if (orgResponse.isError()) {
            //校外访问
            session.setAttribute(SessionConstant.IS_INSIDE, false);
        } else {
            //非校外访问
            session.setAttribute(SessionConstant.IS_INSIDE, true);
            session.setAttribute(SessionConstant.ORG, orgResponse.getBody());
            session.setAttribute(SessionConstant.IP_ORG, orgResponse.getBody());
        }
    }


    /**
     * 清除session
     * @param request
     */
    private void cleanSession(HttpServletRequest request) {
        if (request.getSession().getAttribute(SessionConstant.LOGIN_USER) != null) {
            request.getSession().removeAttribute(SessionConstant.LOGIN_USER);
            request.getSession().removeAttribute(SessionConstant.ORG);
            request.getSession().removeAttribute(SessionConstant.IS_INSIDE);
        }
    }


}
