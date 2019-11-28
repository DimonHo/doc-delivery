package com.wd.cloud.docdelivery.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.constant.SessionConstant;
import com.wd.cloud.commons.exception.AuthException;
import com.wd.cloud.docdelivery.exception.AppException;
import com.wd.cloud.docdelivery.exception.ExceptionEnum;
import com.wd.cloud.docdelivery.model.HelpRequestModel;
import com.wd.cloud.docdelivery.pojo.entity.Permission;
import com.wd.cloud.docdelivery.repository.HelpRecordRepository;
import com.wd.cloud.docdelivery.repository.PermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    HttpServletRequest request;

    private static final List<Integer> PROD_IDS = CollectionUtil.newArrayList(5);

    private static final Long PAPER_CHANNEL = 5L;

    @Pointcut("execution(public * com.wd.cloud.docdelivery.controller.FrontendController.addHelpRecord*(..))")
    public void helpRequest() {
    }

    @Before("helpRequest()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 接收到请求，记录请求内容
        JSONObject sessionUser = (JSONObject) request.getSession().getAttribute(SessionConstant.LOGIN_USER);
        String username = sessionUser != null ? sessionUser.getStr("username") : null;
        JSONObject sessionOrg = (JSONObject) request.getSession().getAttribute(SessionConstant.ORG);
        Integer level = (Integer) request.getSession().getAttribute(SessionConstant.LEVEL);
        Object[] args = joinPoint.getArgs();
        HelpRequestModel helpRequestModel = (HelpRequestModel) args[0];
        Long channel = helpRequestModel.getHelpChannel();

        if (PAPER_CHANNEL.equals(channel)) {
            level = buildLevel(sessionUser, sessionOrg);
        }

        log.info("当前等级：[{}]", level);
        //如果是校外，且未登錄
        if (level < 1) {
            throw new AuthException("校外必须先登录才能求助");
        }
        long helpTotal;
        long helpTotalToday;

        if (StrUtil.isNotBlank(username)) {
            //用户总求助量
            helpTotal = helpRecordRepository.countByHelperName(username);
            helpTotalToday = helpRecordRepository.countByHelperNameToday(username);
            log.info("登陆用户【{}】正在求助", username);
        } else {
            String email = helpRequestModel.getHelperEmail();
            helpTotal = helpRecordRepository.countByHelperEmail(email);
            helpTotalToday = helpRecordRepository.countByHelperEmailToday(email);
            log.info("邮箱【{}】正在求助", email);
        }
        Permission permission = null;
        if (sessionOrg != null) {
            permission = permissionRepository.findByOrgFlagAndLevelAndChannel(sessionOrg.getStr("flag"), level, helpRequestModel.getHelpChannel());
        }
        if (permission == null) {
            permission = permissionRepository.findByOrgFlagIsNullAndLevelAndChannel(level, helpRequestModel.getHelpChannel());
        }
        if (permission != null) {
            if (permission.getTotal() != null && permission.getTotal() <= helpTotal) {
                throw new AppException(ExceptionEnum.HELP_TOTAL_CEILING);
            } else if (permission.getTodayTotal() <= helpTotalToday) {
                throw new AppException(ExceptionEnum.HELP_TOTAL_TODAY_CEILING);
            }
        }
    }

    private Integer buildLevel(JSONObject sessionUser, JSONObject sessionOrg) {
        int paperLevel = (int) request.getSession().getAttribute("paperLevel");
        if (sessionUser == null || sessionUser.isEmpty()) {
            throw new AuthException();
        } else {
            int identityType = sessionUser.getInt("identityType");
            int validStatus = sessionUser.getInt("validStatus");
            if (2 == validStatus){
                paperLevel += 2;
                if (2 == identityType){
                    paperLevel+=4;
                }
            }
        }

        if (sessionOrg != null && !sessionOrg.isEmpty()) {
            JSONArray prodList = sessionOrg.getJSONArray("prodList");
            if (!prodList.isEmpty()) {
                Optional<JSONObject> prodOptional = prodList.toList(JSONObject.class)
                        .stream()
                        .filter(prod -> PROD_IDS.contains(prod.getInt("id")))
                        .findAny();

                if (prodOptional.isPresent()) {
                    JSONObject prod = prodOptional.get();
                    int prodStatus = prod.getInt("status");
                    Date expDate = prod.getDate("expDate");
                    if (1 == prodStatus && new Date().before(expDate)) {
                        paperLevel += 8;
                    }
                }
            }
        }
        return paperLevel;
    }
}
