package com.wd.cloud.docdelivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.constant.SessionConstant;
import com.wd.cloud.commons.exception.NotFoundException;
import com.wd.cloud.commons.util.DateUtil;
import com.wd.cloud.docdelivery.enums.HelpStatusEnum;
import com.wd.cloud.docdelivery.feign.UoServerApi;
import com.wd.cloud.docdelivery.model.AvgResponseTimeModel;
import com.wd.cloud.docdelivery.pojo.dto.MyTjDTO;
import com.wd.cloud.docdelivery.pojo.dto.TjDTO;
import com.wd.cloud.docdelivery.pojo.entity.Permission;
import com.wd.cloud.docdelivery.repository.*;
import com.wd.cloud.docdelivery.service.FrontService;
import com.wd.cloud.docdelivery.service.TjService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.util.*;

/**
 * @author He Zhigang
 * @date 2018/12/18
 * @Description:
 */
@Slf4j
@Service("tjService")
public class TjServiceImpl implements TjService {

    @Autowired
    HelpRecordRepository helpRecordRepository;

    @Autowired
    VHelpRecordRepository vHelpRecordRepository;

    @Autowired
    GiveRecordRepository giveRecordRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    AvgResponseTimeModel avgResponseTimeModel;

    @Autowired
    HelpRawRepository helpRawRepository;

    @Autowired
    UoServerApi uoServerApi;

    @Autowired
    FrontService frontService;

    @Override
    public Map<String, BigInteger> ddcCount(String orgName, String date, int type) {
        String format = DateUtil.formatMysqlStr2(type);
        log.info("orgName={},date={},format={}", orgName, date, format);
        // 如果orgName为空，则查询所有的机构统计
        List<Map<String, Object>> result = orgName != null ? helpRecordRepository.findByOrgNameDdcCount(orgName, date, format)
                : helpRecordRepository.findAllDdcCount(date, format);
        Map<String, BigInteger> ddcCountMap = new HashMap<>();
        result.forEach(rs -> {
            String name = rs.get("orgName") != null ? rs.get("orgName").toString() : "null";
            ddcCountMap.put(name, (BigInteger) rs.get("ddcCount"));
        });
        return ddcCountMap;
    }


    @Override
    public long avgResponseTime(String startDate) {
        // 使用假数据
        return avgResponseTimeModel.getAvgResponseTime();
        // 真实数据
        //return helpRecordRepository.avgResponseDate(startDate);
    }

    @Override
    public long avgSuccessResponseTime(String startDate) {
        // 使用假数据
        return avgResponseTimeModel.getAvgSuccessResponseTime();
        // 真实数据
        //return helpRecordRepository.avgSuccessResponseDate(startDate);
    }

    @Override
    public TjDTO tjForHelp() {
        Map<String, Long> tjResult = helpRecordRepository.tj();
        TjDTO tjDTO = BeanUtil.mapToBean(tjResult, TjDTO.class, true);
        return tjDTO;
    }

    @Override
    public MyTjDTO tjUser(String username, Long channel) {
        Permission permission = getPermission(channel);
        //今日已求助数量
        long myTodayHelpCount = helpRecordRepository.countByHelperNameToday(username, channel);
        //我的总求助数量
        long myHelpCount = helpRecordRepository.countByHelperNameAndHelpChannel(username, channel);
        //我的求助成功数量
        long successHelpCount = helpRecordRepository.countByHelperNameAndHelpChannelAndStatus(username, channel, HelpStatusEnum.HELP_SUCCESSED.value());

        long giveCount = giveRecordRepository.countByGiverName(username);
        //总上限
        Long total = permission.getTotal();
        //每日上限
        Long todayTotal = permission.getTodayTotal();
        //总剩余
        Long restTotal = total == null ? null : total - myHelpCount;
        if (restTotal != null && restTotal < 0) {
            restTotal = 0L;
        }
        // 今日剩余
        Long todayRestTotal = todayTotal == null ? null : todayTotal - myTodayHelpCount;
        //如果今日剩余量大于总剩余量，则今日最多还能求助总剩余数量个
        if (todayRestTotal != null && todayRestTotal < 0) {
            todayRestTotal = 0L;
        }
        todayRestTotal = (todayRestTotal != null && restTotal != null && todayRestTotal > restTotal) ? restTotal : todayRestTotal;
        MyTjDTO myTjDTO = new MyTjDTO();
        myTjDTO.setTotal(total)
                .setTodayTotal(todayTotal)
                .setHelpCount(myHelpCount)
                .setGiveCount(giveCount)
                .setTodayHelpCount(myTodayHelpCount)
                .setRestTotal(restTotal)
                .setTodayRestTotal(todayRestTotal)
                .setSuccessHelpCount(successHelpCount);
        return myTjDTO;
    }

    private Permission getPermission(Long channel) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        HttpSession session = request.getSession();
        Integer level = (Integer) session.getAttribute(SessionConstant.LEVEL);
        JSONObject org = (JSONObject) session.getAttribute(SessionConstant.ORG);
        //如果用户信息中没有机构信息则去IP_ORG中取，都没有则为0（公共配置）
        String orgFlag = org != null ? org.getStr("flag") : null;
        Permission permission = frontService.getPermission(orgFlag, level, channel);
        if (permission == null) {
            throw new NotFoundException("未找到匹配orgFlag=" + orgFlag + ",level=" + level + ",channel=" + channel + "的配置");
        }
        return permission;
    }


    @Override
    public MyTjDTO tjEmail(String email, String ip, Long channel) {
        Permission permission = getPermission(channel);
        long myTodayHelpCount = 0;
        long myHelpCount = 0;
        if (channel == 7){
            //今日已求助数量
             myTodayHelpCount = helpRawRepository.countByHelperNameToday(email, channel);
            //我的总求助数量
             myHelpCount = helpRawRepository.countByHelperNameAndHelpChannel(email, channel);
        }else{
            //今日已求助数量
            myTodayHelpCount = helpRecordRepository.countByHelperEmailToday(email,channel);
            //我的总求助数量
            myHelpCount = helpRecordRepository.countByHelperEmailAndHelpChannel(email,channel);
        }

        long successHelpCount = helpRecordRepository.countByHelperEmailAndHelpChannelAndStatus(email, channel, HelpStatusEnum.HELP_SUCCESSED.value());
        //总上限
        Long total = permission.getTotal();
        //每日上限
        Long todayTotal = permission.getTodayTotal();
        //总剩余
        Long restTotal = total == null ? null : total - myHelpCount;
        if (restTotal != null && restTotal < 0) {
            restTotal = 0L;
        }
        // 今日剩余
        Long todayRestTotal = todayTotal == null ? null : todayTotal - myTodayHelpCount;
        //如果今日剩余量大于总剩余量，则今日最多还能求助总剩余数量个
        if (todayRestTotal != null && todayRestTotal < 0) {
            todayRestTotal = 0L;
        }
        todayRestTotal = (todayRestTotal != null && restTotal != null && todayRestTotal > restTotal) ? restTotal : todayRestTotal;
        MyTjDTO myTjDTO = new MyTjDTO();
        myTjDTO.setTotal(total)
                .setTodayTotal(todayTotal)
                .setHelpCount(myHelpCount)
                .setTodayHelpCount(myTodayHelpCount)
                .setRestTotal(restTotal)
                .setTodayRestTotal(todayRestTotal)
                .setSuccessHelpCount(successHelpCount);
        return myTjDTO;
    }

    @Override
    public List<Map<String, Object>> orgTj(String orgFlag, Integer type, Date begin, Date end) {
        String dateFormat = DateUtil.formatMysqlStr2(type);
        List<Map<String, Object>> res = vHelpRecordRepository.orgTj(orgFlag, dateFormat, begin, end);
        return res;
    }
}
