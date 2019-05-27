package com.wd.cloud.docdelivery.util;

import com.wd.cloud.docdelivery.pojo.entity.LiteraturePlan;
import com.wd.cloud.docdelivery.repository.LiteratureRepository;
import com.wd.cloud.docdelivery.service.LiteraturePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 排班人员工具类
 */
@Component
public class DocDeliveryArrangeUtils {

    @Autowired
    private  static LiteraturePlanService literaturePlanService;

    /**
     * 当天最早排班日期
     */
    private static String startTime;

    /**
     * 当天最晚排班日期
     */
    private static String endTime;

    /**
     * 时分秒格式转换器
     */
    private static SimpleDateFormat sdfMinute;

    /**
     * 当天排班人员
     */
    private static List<LiteraturePlan> userNames;

    /**
     * 上一个值班的人
     */
    private static String username;

    private static Calendar calendar;

    static {
        sdfMinute = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        calendar = Calendar.getInstance();
    }

    /**
     * 返回排班人员
     * @return
     */
    public static LiteraturePlan getUserName() {
        //获取当天日期,查询排班人员
        Date date = new Date();
        String nowDate = sdfMinute.format(date);
        // 如果日期大于当前日期或者排班日期为空,则默认代替
        if (endTime == null || nowDate.compareTo(endTime) > 0) {
            // 如果当天时间为空的,则获取当天列表
            if (endTime == null) {
                userNames = literaturePlanService.findByDate(calendar.getTime());
            }
            // 判断当前时间是否大于当天最晚时间
            if (userNames.size() > 0) {
                // 初始化最晚时间
                endTime = sdfMinute.format(userNames.get(userNames.size() - 1).getEndTime());
                // 判断当前时间是否大于今天最晚值班人员下班时间
                if (nowDate.compareTo(endTime) > 0) {
                    calendar.setTime(date);
                    calendar.add(Calendar.DATE,1);
                    //获取明天的值班人员
                    userNames = literaturePlanService.findByDate(calendar.getTime());
                }
            }
            // 对时间进行初始化
            if (userNames.size() > 0) {
                endTime = sdfMinute.format(userNames.get(userNames.size() - 1).getEndTime());
                // 排序获取最早时间
                userNames = sort(userNames);
                startTime = sdfMinute.format(userNames.get(0).getStartTime());
            }

        }
        //定义封装符合条件的容器
        List<LiteraturePlan> matchTimeUserNames = new ArrayList<>(10);
        //这里可以默认条件,如果目前时间大于最后一个,直接
        if(nowDate.compareTo(endTime) < 0 && nowDate.compareTo(startTime) > 0) {
            //首先查询符合条件的人
            for (LiteraturePlan literaturePlan : userNames) {
                if (sdfMinute.format(literaturePlan.getStartTime()).compareTo(nowDate) <= 0
                        && sdfMinute.format(literaturePlan.getEndTime()).compareTo(nowDate) > 0) {
                    matchTimeUserNames.add(literaturePlan);
                }
            }
        }
        // 如果不符合条件,则判断时间进行排班

        if (userNames.size() > 0 && matchTimeUserNames.size() == 0) {
            // 如果值班人员下班,默认将任务分配最早值班的人
            if (nowDate.compareTo(startTime) < 0) {
                // 获取符合条件的排班人
                for (int i = 0; i < userNames.size(); i ++) {
                    // 如果按照开始时间排序的人与最早时间相等,则加入,否则跳出
                    if(startTime.equals(sdfMinute.format(userNames.get(i).getStartTime()))) {
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
        for (int i = 0; i < array.size()-1; i++){//外层循环控制排序趟数
            for (int j = 0; j < array.size()-1-i; j++){//内层循环控制每一趟排序多少次
                if (array.get(j).getStartTime().compareTo(array.get(j+1).getStartTime()) >= 0){
                    LiteraturePlan temp = array.get(j);
                    array.set(j, array.get(j + 1));
                    array.set(j + 1, temp);
                }
            }
        }
        return array;
    }

}
