package com.wd.cloud.docdelivery.util;

import com.wd.cloud.docdelivery.pojo.entity.LiteraturePlan;
import com.wd.cloud.docdelivery.service.LiteraturePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 排班人员工具类
 */
@Component
public class DocDeliveryArrangeUtils {

    @Autowired
    private static LiteraturePlanService literaturePlanService;

    /**
     * 排班日期
     */
    private static String arrangeDate;

    /**
     * 格式转换器
     */
    private static SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");

    /**
     * 时分秒格式转换器
     */
    private static SimpleDateFormat sdfMinute = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
     *
     * @return
     */
    public static LiteraturePlan getUserName() {
        //获取当天日期,查询排班人员
        String nowDate = sdf.format(new Date());
        //如果日期大于当前日期或者排班日期为空,则默认代替
        if (arrangeDate == null || nowDate.compareTo(arrangeDate) > 0) {
            //查询排班人员并且赋值返回
            userNames = literaturePlanService.findByDate();
            //将位置置为0,日期进行设值
            arrangeDate = nowDate;
        }
        //定义封装符合条件的容器
        List<LiteraturePlan> matchTimeUserNames = new ArrayList<>();
        Date date = new Date();
        //首先查询符合条件的人
        for (LiteraturePlan literaturePlan : userNames) {
            String start = sdfMinute.format(literaturePlan.getStartTime());
            String local = sdfMinute.format(date);
            if (sdfMinute.format(literaturePlan.getStartTime()).compareTo(sdfMinute.format(date)) < 0
                    && sdfMinute.format(literaturePlan.getEndTime()).compareTo(sdfMinute.format(date)) > 0) {
                matchTimeUserNames.add(literaturePlan);
            }
        }

        //查询排班人员
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
        return null;

    }


    @Autowired(required = true)
    public void setLiteraturePlanService(LiteraturePlanService literaturePlanService) {
        DocDeliveryArrangeUtils.literaturePlanService = literaturePlanService;
    }

}
