package com.wd.cloud.docdelivery.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: He Zhigang
 * @Date: 2019/11/29 16:24
 * @Description:
 */
public class GlobalConstants {
    /**
     * 渠道ID和产品ID对应关系
     */
    public static final Map<Long, Integer> CHANNEL_TO_PROD = MapUtil.builder(new HashMap<Long, Integer>())
            .put(5L, 5)
            .put(7L, 7)
            .build();

    /**
     * 文献互助平台和微信小程序
     */
    public static final List<Long> SECOND_CHANNELS = CollectionUtil.toList(5L, 7L);

    /**
     * 已实名认证状态值
     */
    public static final Integer VERIFIED = 2;

    /**
     * 教师身份
     */
    public static final Integer TEACHER = 2;
    /**
     * 购买状态
     */
    public static final Integer BUY = 1;
}
