package com.wd.cloud.docdelivery.service;

import javax.servlet.http.HttpSession;

/**
 * @Author: He Zhigang
 * @Date: 2019/11/29 10:33
 * @Description:
 */
public interface BuildLevelService {

    /**
     * 构建权限等级
     * @param session
     * @param username
     * @param channel
     * @return
     */
    int buildLevel(HttpSession session, String username, Long channel);

}
