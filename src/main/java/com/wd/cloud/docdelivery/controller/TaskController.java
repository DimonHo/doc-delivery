package com.wd.cloud.docdelivery.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.model.AvgResponseTimeModel;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import com.wd.cloud.docdelivery.repository.VHelpRecordRepository;
import com.wd.cloud.docdelivery.service.MailService;
import com.wd.cloud.docdelivery.service.TaskService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @Author: He Zhigang
 * @Date: 2019/8/27 16:42
 * @Description:
 */
@Slf4j
@Api(value = "定时任务接口", tags = {"定时任务"})
@RestController
public class TaskController {

    @Autowired
    TaskService taskService;

    @Autowired
    AvgResponseTimeModel avgResponseTimeModel;

    @Autowired
    VHelpRecordRepository vHelpRecordRepository;

    @Autowired
    MailService mailService;

    /**
     * 用户应助超时，每隔15秒扫描一次
     * @Scheduled(fixedRate = 1000 * 60 * 15)
     * @return
     */
    @GetMapping("/task/giving/timeout")
    public ResponseModel giveTimeout() {
        taskService.giveTimeout();
        return ResponseModel.ok();
    }

    /**
     * 平均响应时间，每隔1小时变更一次
     * @Scheduled(cron = "0 0/1 * * * ?")
     * @return
     */
    @GetMapping("/task/avg-resp-time")
    public ResponseModel avgRespTime() {
        long min = Math.round(avgResponseTimeModel.getAvgResponseTime() * 0.9);
        long max = Math.round(avgResponseTimeModel.getAvgResponseTime() * 1.1);
        min = min < 50 ? 50 : min;
        max = max > 120 ? 120 : max;
        avgResponseTimeModel.setAvgResponseTime(RandomUtil.randomLong(min, max));
        long minSuccess = Math.round(avgResponseTimeModel.getAvgSuccessResponseTime() * 0.9);
        long maxSuccess = Math.round(avgResponseTimeModel.getAvgSuccessResponseTime() * 1.1);
        minSuccess = minSuccess < 145 ? 145 : minSuccess;
        maxSuccess = maxSuccess > 325 ? 325 : maxSuccess;
        avgResponseTimeModel.setAvgSuccessResponseTime(RandomUtil.randomLong(minSuccess, maxSuccess));
        return ResponseModel.ok().setBody(avgResponseTimeModel);
    }

    /**
     * 失败的邮件重新发送
     * @Scheduled(cron = "0/15 * * * * ?")
     * @return
     */
    @GetMapping("/task/mail/resend")
    public ResponseModel sendMail(){
        // 2分钟前的未发送
        Date gmtModified = DateUtil.offsetMinute(new Date(), -2).toJdkDate();
        List<Integer> sendStatus = CollectionUtil.newArrayList(3, 4);
        List<VHelpRecord> bySend = vHelpRecordRepository.findBySendAndGmtModifiedBeforeAndStatusIn(false, gmtModified, sendStatus);
        String businessId = "";
        if (CollectionUtil.isNotEmpty(bySend)) {
            VHelpRecord vHelpRecord = RandomUtil.randomEle(bySend);
            businessId = vHelpRecord.getId() + "-" + vHelpRecord.getStatus() + "-" + vHelpRecord.getDifficult();
            log.info("重新发送邮件：{}", businessId);
            mailService.sendMail(vHelpRecord);
        }
        return ResponseModel.ok().setBody(businessId);
    }
}
