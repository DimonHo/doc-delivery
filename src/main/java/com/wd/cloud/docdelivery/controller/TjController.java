package com.wd.cloud.docdelivery.controller;

import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.annotation.ValidateLogin;
import com.wd.cloud.commons.constant.SessionConstant;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.commons.util.DateUtil;
import com.wd.cloud.docdelivery.pojo.dto.MyTjDto;
import com.wd.cloud.docdelivery.pojo.dto.TjDto;
import com.wd.cloud.docdelivery.service.TjService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

/**
 * @author He Zhigang
 * @date 2018/12/18
 * @Description:
 */
@Api(value = "统计分析controller", tags = {"文献传递数据统计分析"})
@RestController
public class TjController {

    @Autowired
    TjService tjService;

    @Autowired
    HttpServletRequest request;

    @ApiOperation(value = "邮箱统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "用户邮箱", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "channel", value = "求助渠道", dataType = "Long", paramType = "query")
    })
    @GetMapping("/tj")
    public ResponseModel getEmailHelpCountToDay(@RequestParam String email, @RequestParam(required = false) Long channel) {
        MyTjDto myTotalModel = tjService.tjEmail(email, ServletUtil.getClientIP(request), channel);
        return ResponseModel.ok().setBody(myTotalModel);
    }

    @ApiOperation(value = "我的统计")
    @ApiImplicitParam(name = "channel", value = "求助渠道", dataType = "Long", paramType = "query")
    @ValidateLogin
    @GetMapping("/tj/my")
    public ResponseModel getUserHelpCountToDay(@RequestParam(required = false) Long channel) {

        JSONObject loginUser = (JSONObject) request.getSession().getAttribute(SessionConstant.LOGIN_USER);
        String username = loginUser != null ? loginUser.getStr("username") : null;
        MyTjDto myTotalModel = tjService.tjUser(username, channel);
        return ResponseModel.ok().setBody(myTotalModel);

    }

    @ApiOperation(value = "文献传递量统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orgName", value = "机构全称", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "date", value = "统计时间", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "0:按秒统计，1:按分钟统计（默认），2：按小时统计，3：按天统计，4：按月统计，5：按年统计", dataType = "Integer", paramType = "query")
    })
    @GetMapping("/ddc_count/name")
    public ResponseModel ddcCountByOrgName(@RequestParam(required = false) String orgName,
                                           @RequestParam(required = false) String date,
                                           @RequestParam(required = false, defaultValue = "1") Integer type) {
        date = date != null ? date : DateUtil.now();
        Map<String, BigInteger> body = tjService.ddcCount(orgName, date, type);
        return ResponseModel.ok().setBody(body);
    }


    @ApiOperation(value = "获取平台总求助量、成功量、成功率、今日求助量")
    @GetMapping("/tj/total")
    public ResponseModel getHeadTotalFor() {
        TjDto body = tjService.tjForHelp();
        return ResponseModel.ok().setBody(body);
    }

    @ApiOperation(value = "获取平均响应时间")
    @ApiImplicitParam(name = "startDate", value = "起始统计时间", dataType = "String", paramType = "query")
    @GetMapping("/tj/avg-time")
    public ResponseModel avgTime(@RequestParam(required = false, defaultValue = "2019-01-01 00:00:00") String startDate) {
        long avgResponseTime = tjService.avgResponseTime(startDate);
        long avgSuccessResponseTime = tjService.avgSuccessResponseTime(startDate);
        JSONObject avgResponseJson = new JSONObject();
        avgResponseJson.put("avgResponseTime", avgResponseTime);
        avgResponseJson.put("avgSuccessResponseTime", avgSuccessResponseTime);
        return ResponseModel.ok().setBody(avgResponseJson);
    }


    @ApiOperation(value = "机构统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "统计类型，0（默认）：时分秒，1：按分钟统计，2按小时统计，3按日统计，4按月统计，5按年统计", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "begin", value = "起始时间", dataType = "Date", paramType = "query"),
            @ApiImplicitParam(name = "end", value = "结束时间", dataType = "Date", paramType = "query")
    })
    @GetMapping("/tj/org")
    public ResponseModel orgTj(@RequestParam(required = false, defaultValue = "3") Integer type,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date begin,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end) {
        end = end == null ? DateUtil.endOfDay(new Date()).toJdkDate() : end;
        // 默认范围一个月
        begin = begin == null ? DateUtil.offsetMonth(end, -1).toJdkDate() : begin;
        JSONObject org = (JSONObject) request.getSession().getAttribute(SessionConstant.ORG);
        if (org != null) {
            String orgFlag = org.getStr("flag");
            return ResponseModel.ok().setBody(tjService.orgTj(orgFlag, type, begin, end));
        }

        return ResponseModel.fail().setMessage("访问IP非法");
    }

}
