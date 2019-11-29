package com.wd.cloud.docdelivery.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wd.cloud.commons.constant.SessionConstant;
import com.wd.cloud.commons.exception.AuthException;
import com.wd.cloud.docdelivery.exception.AppException;
import com.wd.cloud.docdelivery.exception.ExceptionEnum;
import com.wd.cloud.docdelivery.pojo.entity.Permission;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import com.wd.cloud.docdelivery.repository.PermissionRepository;
import com.wd.cloud.docdelivery.service.BuildLevelService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author He Zhigang
 * @date 2019/1/18
 * @Description: 求助拦截，求助前检查用户是否还有求助次数
 */
@Slf4j
@Aspect
@Component
@Order(9)
public class HelpRequestAspect {

    @Autowired
    HelpRecordRepository helpRecordRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    BuildLevelService buildLevelService;

    @Autowired
    HttpServletRequest request;

    @Pointcut("execution(public * com.wd.cloud.docdelivery.controller.FrontendController.addHelpRecord*(..))")
    public void helpRequest() {
    }

    @Before("helpRequest()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 接收到请求，记录请求内容
        HttpSession session = request.getSession();
        Object[] args = joinPoint.getArgs();
        JSONObject helpParams = (JSONObject) args[0];
        String username = helpParams.getStr("username");
        JSONObject userSession = JSONUtil.parseObj(session.getAttribute(SessionConstant.LOGIN_USER));
        if (username == null && !userSession.isEmpty()) {
            username = userSession.getStr("username");
        }
        Long channel = helpParams.getLong("helperChannel");
        String helperEmail = helpParams.getStr("helperEmail");
        int level = buildLevelService.buildLevel(session, username, channel);

        log.info("当前渠道[{}]等级：[{}]", channel, level);
        if (level < 1) {
            throw new AuthException("未登录");
        }
        long helpTotal;
        long helpTotalToday;

        if (StrUtil.isNotBlank(username)) {
            //用户总求助量
            helpTotal = helpRecordRepository.countByHelperNameAndHelpChannel(username, channel);
            helpTotalToday = helpRecordRepository.countByHelperNameToday(username, channel);
            log.info("登陆用户【{}】正在求助", username);
        } else {
            helpTotal = helpRecordRepository.countByHelperEmailAndHelpChannel(helperEmail, channel);
            helpTotalToday = helpRecordRepository.countByHelperEmailToday(helperEmail, channel);
            log.info("邮箱【{}】正在求助", helperEmail);
        }
        Permission permission = null;
        JSONObject orgSession = JSONUtil.parseObj(session.getAttribute(SessionConstant.ORG));
        if (!orgSession.isEmpty()) {
            permission = permissionRepository.findByOrgFlagAndLevelAndChannel(orgSession.getStr("flag"), level, channel);
        }
        if (permission == null) {
            permission = permissionRepository.findByOrgFlagIsNullAndLevelAndChannel(level, channel);
        }
        if (permission != null) {
            if (permission.getTotal() != null && permission.getTotal() <= helpTotal) {
                throw new AppException(ExceptionEnum.HELP_TOTAL_CEILING);
            } else if (permission.getTodayTotal() <= helpTotalToday) {
                throw new AppException(ExceptionEnum.HELP_TOTAL_TODAY_CEILING);
            }
        }
    }

}
