package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wd.cloud.commons.constant.SessionConstant;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.config.GlobalConstants;
import com.wd.cloud.docdelivery.exception.AppException;
import com.wd.cloud.docdelivery.feign.UoServerApi;
import com.wd.cloud.docdelivery.repository.HelpRawRepository;
import com.wd.cloud.docdelivery.repository.PermissionRepository;
import com.wd.cloud.docdelivery.service.BuildLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @Author: He Zhigang
 * @Date: 2019/11/29 10:34
 * @Description:
 */
@Slf4j
@Service("buildLevelService")
public class BuildLevelServiceImpl implements BuildLevelService {

    @Autowired
    HelpRawRepository helpRawRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    UoServerApi uoServerApi;

    @Override
    public int buildLevel(HttpSession session, String username, Long channel) {
        if (GlobalConstants.SECOND_CHANNELS.contains(channel)) {
            return buildSecondLevel(session, username, channel);
        } else {
            return buildFirstLevel(session);
        }
    }

    private int buildFirstLevel(HttpSession session) {
        int level = 0;
        JSONObject userSession = JSONUtil.parseObj(session.getAttribute(SessionConstant.LOGIN_USER));
        boolean isInside = (boolean) session.getAttribute(SessionConstant.IS_INSIDE);
        // 校内
        if (isInside) {
            level += 1;
        }
        // 已登录
        if (!userSession.isEmpty()) {
            level += 2;
            Integer validStatus = userSession.getInt("validStatus");
            if (GlobalConstants.VERIFIED.equals(validStatus)) {
                level += 4;
            }
        }
        return level;
    }

    private int buildSecondLevel(HttpSession session, String username, Long channel) {
        int level = 0;
        if (StrUtil.isNotBlank(username)) {
            JSONObject userSession = JSONUtil.parseObj(session.getAttribute(SessionConstant.LOGIN_USER));
            boolean userIdChange = !username.equals(userSession.getStr("username"))
                    && !username.equals(userSession.getStr("email"));
            if (userSession.isEmpty() || userIdChange) {
                ResponseModel<JSONObject> userResponse = uoServerApi.user(username);
                if (userResponse.isError()) {
                    throw new AppException(userResponse.getStatus(), userResponse.getMessage());
                }
                userSession = userResponse.getBody();
                session.setAttribute(SessionConstant.LOGIN_USER, userSession);
            }
            level += 8;
            String orgFlag = userSession.getStr("orgFlag");
            // 如果用户所属某个机构，则以该机构作为访问机构
            if (StrUtil.isNotBlank(orgFlag)) {
                JSONObject orgSession = JSONUtil.parseObj(session.getAttribute(SessionConstant.ORG));
                if (orgSession.isEmpty() || !orgFlag.equals(orgSession.getStr("flag"))) {
                    ResponseModel<JSONObject> orgResponse = uoServerApi.org(null, orgFlag, null);
                    if (orgResponse.isError()) {
                        throw new AppException(orgResponse.getStatus(), orgResponse.getMessage());
                    }
                    orgSession = orgResponse.getBody();
                    session.setAttribute(SessionConstant.ORG, orgSession);
                }
                // 获取用户的机构信息
                if (!orgSession.isEmpty()) {
                    level += 1;
                    // 身份：教师 or 学生
                    Integer identityType = userSession.getInt("identityType");
                    if (GlobalConstants.TEACHER.equals(identityType)) {
                        level += 2;
                    }
                    // 证件照验证状态 2为已验证
                    Integer validStatus = userSession.getInt("validStatus");
                    if (GlobalConstants.VERIFIED.equals(validStatus)) {
                        level += 4;
                        JSONArray prodList = orgSession.getJSONArray("prodList");
                        if (!prodList.isEmpty()) {
                            Optional<JSONObject> prodOptional = prodList.toList(JSONObject.class)
                                    .stream()
                                    .filter(prod -> GlobalConstants.CHANNEL_TO_PROD.get(channel).equals(prod.getInt("id")))
                                    .findAny();

                            if (prodOptional.isPresent()) {
                                JSONObject prod = prodOptional.get();
                                int prodStatus = prod.getInt("status");
                                Date expDate = prod.getDate("expDate");
                                if (GlobalConstants.BUY == prodStatus && new Date().before(expDate)) {
                                    level += 8;
                                }
                            }
                        }
                    }

                }
            }
        }
        return level;
    }
}
