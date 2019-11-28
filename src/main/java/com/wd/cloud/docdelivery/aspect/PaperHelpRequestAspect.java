package com.wd.cloud.docdelivery.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.constant.SessionConstant;
import com.wd.cloud.commons.exception.AuthException;
import com.wd.cloud.commons.exception.NotFoundException;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.exception.AppException;
import com.wd.cloud.docdelivery.exception.ExceptionEnum;
import com.wd.cloud.docdelivery.feign.UoServerApi;
import com.wd.cloud.docdelivery.model.HelpRawModel;
import com.wd.cloud.docdelivery.pojo.entity.Permission;
import com.wd.cloud.docdelivery.repository.HelpRawRepository;
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
 * @Author: He Zhigang
 * @Date: 2019/11/28 16:41
 * @Description:
 */
@Slf4j
@Aspect
@Component
@Order(9)
public class PaperHelpRequestAspect {

    @Autowired
    HelpRawRepository helpRawRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    HttpServletRequest request;

    @Autowired
    UoServerApi uoServerApi;

    private static final List<Integer> PROD_IDS = CollectionUtil.newArrayList( 7);

    private static final List<Long> PAPER_CHANNEL = CollectionUtil.newArrayList( 7L);

    private static final Integer VERIFIED = 2, TEACHER = 2, BUY = 1;


    @Pointcut("execution(public * com.wd.cloud.docdelivery.controller.FrontendController.addHelpRaw(..))")
    public void helpRequest() {
    }

    @Before("helpRequest()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 接收到请求，记录请求内容
        Object[] args = joinPoint.getArgs();
        HelpRawModel helpRawModel = (HelpRawModel) args[0];
        String username = helpRawModel.getHelperName();
        if (StrUtil.isBlank(username)) {
            throw new AuthException();
        }
        Long channel = helpRawModel.getHelpChannel();
        int level = 0;
        if (PAPER_CHANNEL.contains(channel)) {
            level = buildLevel(username);
        }
        log.debug("当前等级：[{}]", level);
        // 总已求助量
        long helpTotal = helpRawRepository.countByHelperNameAndHelpChannel(username,channel);
        // 今日已求助量
        long helpTotalToday = helpRawRepository.countByHelperNameToday(username,channel);

        Permission permission = null;
        JSONObject sessionOrg = (JSONObject) request.getSession().getAttribute(SessionConstant.ORG);
        if (sessionOrg != null) {
            permission = permissionRepository.findByOrgFlagAndLevelAndChannel(sessionOrg.getStr("flag"), level, channel);
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

    private int buildLevel(String username) {
        int paperLevel = (Integer) request.getSession().getAttribute(SessionConstant.LEVEL) == null ?
                0 : (Integer) request.getSession().getAttribute(SessionConstant.LEVEL);
        if (paperLevel == 0) {
            ResponseModel<JSONObject> userResponse = uoServerApi.user(username);
            log.debug("调用uo-server获取用户信息【{}】", userResponse);
            if (!userResponse.isError()) {
                paperLevel += 8;
                JSONObject userInfo = userResponse.getBody();
                // 证件照验证状态 2为已验证
                Integer validStatus = userInfo.getInt("validStatus");
                // 身份：教师 or 学生
                Integer identityType = userInfo.getInt("identityType");
                if (VERIFIED.equals(validStatus)) {
                    paperLevel += 1;
                    if (TEACHER.equals(identityType)) {
                        paperLevel += 2;
                    }
                }
                String orgFlag = userInfo.getStr("orgFlag");
                // 如果用户所属某个机构，则以该机构作为访问机构
                if (StrUtil.isNotBlank(orgFlag)) {
                    // 获取用户的机构信息
                    ResponseModel<JSONObject> orgFlagResponse = uoServerApi.org(null, orgFlag, null);
                    log.debug("调用uo-server获取【{}】用户的机构信息【{}】", username, orgFlagResponse);
                    if (!orgFlagResponse.isError()) {
                        JSONObject orgInfo = orgFlagResponse.getBody();
                        paperLevel += 4;
                        JSONArray prodList = orgInfo.getJSONArray("prodList");
                        if (!prodList.isEmpty()) {
                            Optional<JSONObject> prodOptional = prodList.toList(JSONObject.class)
                                    .stream()
                                    .filter(prod -> PROD_IDS.contains(prod.getInt("id")))
                                    .findAny();

                            if (prodOptional.isPresent()) {
                                JSONObject prod = prodOptional.get();
                                int prodStatus = prod.getInt("status");
                                Date expDate = prod.getDate("expDate");
                                if (BUY == prodStatus && new Date().before(expDate)) {
                                    paperLevel += 8;
                                }
                            }
                        }
                        request.getSession().setAttribute(SessionConstant.ORG, orgInfo);
                    }
                }
                request.getSession().setAttribute(SessionConstant.LOGIN_USER, userInfo);
                request.getSession().setAttribute(SessionConstant.LEVEL, paperLevel);
            } else {
                throw new NotFoundException(userResponse.getMessage());
            }
        }
        return paperLevel;
    }
}
