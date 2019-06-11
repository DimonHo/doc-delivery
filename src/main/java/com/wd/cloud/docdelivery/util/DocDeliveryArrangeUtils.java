package com.wd.cloud.docdelivery.util;

import cn.hutool.core.collection.CollectionUtil;
import com.wd.cloud.commons.util.DateUtil;
import com.wd.cloud.docdelivery.pojo.entity.LiteraturePlan;
import com.wd.cloud.docdelivery.service.LiteraturePlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 排班人员工具类
 */
@Slf4j
@Component
public class DocDeliveryArrangeUtils {

    @Autowired
    private  static LiteraturePlanService literaturePlanService;

    /**
     * 当天最早排班日期
     */
    private static Date startTime;

    /**
     * 当天最晚排班日期
     */
    private static Date endTime;

    /**
     * 当天排班人员
     */
    private static List<LiteraturePlan> userNames;

    /**
     * 上一个值班的人
     */
    private static String username;

    /**
     * 返回排班人员
     * @return
     */
    public static LiteraturePlan getUserName() {
        //获取当天日期,查询排班人员
        Date now = new Date();
        // 如果日期大于当前日期或者排班日期为空,则默认代替
        if (endTime == null || now.after(endTime)) {
            // 如果当天时间为空的,则获取当天列表
            if (endTime == null) {
                log.info("查询排版人员的时间：{}", DateUtil.formatDateTime(now));
                userNames = literaturePlanService.findNowDaysLiteraturePlans();
            }
            // 判断当前时间是否大于当天最晚时间
            if (CollectionUtil.isNotEmpty(userNames)) {
                // 初始化最晚时间
                endTime = userNames.get(userNames.size() - 1).getEndTime();
                // 判断当前时间是否大于今天最晚值班人员下班时间
                if (now.after(endTime)) {
                    //获取明天的值班人员
                    userNames = literaturePlanService.findNextLiteraturePlans();
                    log.info("查询排版人员的时间：{}", userNames.get(0).getEndTime());
                }
            }
            // 对时间进行初始化
            if (CollectionUtil.isNotEmpty(userNames)) {
                endTime = userNames.get(userNames.size() - 1).getEndTime();
                // 排序获取最早时间
                userNames = sort(userNames);
                startTime = userNames.get(0).getStartTime();
            }

        }
        //定义封装符合条件的容器
        List<LiteraturePlan> matchTimeUserNames = new ArrayList<>(10);
        if (CollectionUtil.isNotEmpty(userNames)) {
            //这里可以默认条件,如果目前时间大于最后一个,直接
            if (now.before(endTime) && now.after(startTime)) {
                //首先查询符合条件的人
                for (LiteraturePlan literaturePlan : userNames) {
                    if (literaturePlan.getStartTime().compareTo(now) <= 0
                            && literaturePlan.getEndTime().compareTo(now) >= 0) {
                        matchTimeUserNames.add(literaturePlan);
                    }
                }
            }
            // 如果值班人员下班,默认将任务分配最早值班的人
            if (now.compareTo(startTime) < 0) {
                // 获取符合条件的排班人
                for (int i = 0; i < userNames.size(); i ++) {
                    // 如果按照开始时间排序的人与最早时间相等,则加入,否则跳出
                    if(startTime.equals(userNames.get(i).getStartTime())) {
                        matchTimeUserNames.add(userNames.get(i));
                    } else {
                        break;
                    }
                }
            }
        }
        // 查询排班人员
        for (int i = 0; i < matchTimeUserNames.size(); i++) {
            if (matchTimeUserNames.get(i).getUsername().equals(username)) {
                //如果查到,且不是最后一个,则返回下一个,如果是,则返回第一个
                if (i != matchTimeUserNames.size() - 1) {
                    username = matchTimeUserNames.get(i + 1).getUsername();
                    return matchTimeUserNames.get(i + 1);
                }
            }
            //如果没找到,且到了最后一个,默认返回第一个
            if (i == matchTimeUserNames.size() - 1) {
                username = matchTimeUserNames.get(0).getUsername();
                return matchTimeUserNames.get(0);
            }

        }
        // 如果时间有空缺,或者没有安排人员值班会出现此问题
        return null;

    }


    @Autowired(required = true)
    public  void setLiteraturePlanService(LiteraturePlanService literaturePlanService) {
        DocDeliveryArrangeUtils.literaturePlanService = literaturePlanService;
    }


    private static List<LiteraturePlan> sort(List<LiteraturePlan> array) {
        //外层循环控制排序趟数
        for (int i = 0; i < array.size()-1; i++){
            //内层循环控制每一趟排序多少次
            for (int j = 0; j < array.size()-1-i; j++){
                if (array.get(j).getStartTime().compareTo(array.get(j+1).getStartTime()) >= 0){
                    LiteraturePlan temp = array.get(j);
                    array.set(j, array.get(j + 1));
                    array.set(j + 1, temp);
                }
            }
        }
        log.debug("开始时间排序结果：{}", CollectionUtil.join(array,","));
        return array;
    }

}
