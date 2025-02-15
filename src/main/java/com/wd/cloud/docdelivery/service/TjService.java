package com.wd.cloud.docdelivery.service;

import com.wd.cloud.docdelivery.pojo.dto.MyTjDto;
import com.wd.cloud.docdelivery.pojo.dto.TjDto;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author He Zhigang
 * @date 2018/12/18
 * @Description:
 */
public interface TjService {

    /**
     * 实时文献传递量
     *
     * @param orgName
     * @param date
     * @param type
     * @return
     */
    Map<String, BigInteger> ddcCount(String orgName, String date, int type);

    /**
     * 求助统计
     *
     * @return
     */
    TjDto tjForHelp();


    /**
     * 平均响应时间
     *
     * @param startDate
     * @return
     */
    long avgResponseTime(String startDate);

    /**
     * 成功的平均响应时间
     *
     * @param startDate
     * @return
     */
    long avgSuccessResponseTime(String startDate);

    /**
     * 我的统计
     *
     * @param username
     * @param channel
     * @return
     */
    MyTjDto tjUser(String username, Long channel);

    /**
     * 邮箱的统计
     *
     * @param email
     * @param ip
     * @param channel
     * @return
     */
    MyTjDto tjEmail(String email, String ip, Long channel);


    /**
     *
     * @param orgFlag
     * @param type
     * @param begin
     * @param end
     * @return
     */
    List<Map<String, Object>> orgTj(String orgFlag, Integer type, Date begin, Date end);

}
