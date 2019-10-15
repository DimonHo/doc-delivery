package com.wd.cloud.docdelivery.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.annotation.ValidateLogin;
import com.wd.cloud.commons.constant.SessionConstant;
import com.wd.cloud.commons.enums.StatusEnum;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.AppContextUtil;
import com.wd.cloud.docdelivery.config.Global;
import com.wd.cloud.docdelivery.exception.AppException;
import com.wd.cloud.docdelivery.exception.ExceptionEnum;
import com.wd.cloud.docdelivery.model.HelpRequestModel;
import com.wd.cloud.docdelivery.pojo.dto.GiveRecordDTO;
import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.pojo.entity.HelpRecord;
import com.wd.cloud.docdelivery.pojo.entity.Literature;
import com.wd.cloud.docdelivery.pojo.entity.Permission;
import com.wd.cloud.docdelivery.service.FrontService;
import com.wd.cloud.docdelivery.service.HelpRequestService;
import com.wd.cloud.docdelivery.service.MailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author He Zhigang
 * @date 2018/5/3
 */
@Slf4j
@Api(value = "前台controller", tags = {"前台文献互助接口"})
@RestController
@RequestMapping("/front")
public class FrontendController {

    @Autowired
    Global global;

    @Autowired
    MailService mailService;

    @Autowired
    FrontService frontService;

    @Autowired
    HelpRequestService helpRequestService;

    @Autowired
    HttpServletRequest request;

    @ApiOperation(value = "文献求助 json参数",tags = {"文献求助"})
    @PostMapping(value = "/help/record")
    public ResponseModel<HelpRecord> addHelpRecord1(@Valid @RequestBody HelpRequestModel helpRequestModel){
        log.info("求助体" + helpRequestModel.toString());
        return helpRequest(helpRequestModel);
    }

    @ApiOperation(value = "文献求助 from表单",tags = {"文献求助"})
    @PostMapping(value = "/help/form")
    public ResponseModel<HelpRecord> addHelpRecord2(@Valid HelpRequestModel helpRequestModel) {
        return helpRequest(helpRequestModel);
    }

    private ResponseModel<HelpRecord> helpRequest(HelpRequestModel helpRequestModel) {
        JSONObject loginUser = (JSONObject) request.getSession().getAttribute(SessionConstant.LOGIN_USER);
        String username = loginUser != null ? loginUser.getStr("username") : null;
        JSONObject org = (JSONObject) request.getSession().getAttribute(SessionConstant.ORG);
        String ip = ServletUtil.getClientIP(request);
        HelpRecord helpRecord = BeanUtil.toBean(helpRequestModel, HelpRecord.class);
        Literature literature = BeanUtil.toBean(helpRequestModel, Literature.class);

        if (StrUtil.isNotBlank(username)) {
            helpRecord.setHelperName(username);
        }
        if (org != null) {
            helpRecord.setOrgFlag(org.getStr("flag")).setOrgName(org.getStr("name"));
        } else {
            helpRecord.setOrgFlag(helpRequestModel.getOrgFlag()).setOrgName(helpRequestModel.getOrgName());
        }
        helpRecord.setHelperIp(ip);
        try {
            helpRequestService.helpRequest(literature, helpRecord);
            return ResponseModel.ok().setMessage("求助成功");
        } catch (ConstraintViolationException e) {
            throw new AppException(ExceptionEnum.HELP_REPEAT);
        }
    }

    @ApiOperation(value = "查询求助记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "channel", value = "求助渠道，0:paper平台，1：QQ,2:SPIS,3:智汇云，4：CRS", dataType = "List", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "过滤状态，0：待应助， 1：应助中（用户已认领，15分钟内上传文件）， 2: 待审核（用户已应助）， 3：求助第三方（第三方应助）， 4：应助成功（审核通过或管理员应助）， 5：应助失败（超过15天无结果）", dataType = "List", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "模糊查询", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "email", value = "邮箱过滤", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "isDifficult", value = "是否是疑难文献", dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "isOrg", value = "只显示本校(默认false,查询所有)", dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "beginTime", value = "起始时间（默认最近一周）", dataType = "Date", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "Date", paramType = "query")
    })
    @GetMapping("/help/records")
    public ResponseModel helpRecords(@RequestParam(required = false) List<Long> channel,
                                     @RequestParam(required = false) List<Integer> status,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String email,
                                     @RequestParam(required = false) Boolean isDifficult,
                                     @RequestParam(required = false, defaultValue = "false") boolean isOrg,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                     @PageableDefault(sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable) {
        JSONObject org = (JSONObject) request.getSession().getAttribute(SessionConstant.ORG);
        String orgFlag = org != null && isOrg ? org.getStr("flag") : null;
        Page<HelpRecordDTO> helpRecordDTOS = frontService.getHelpRecords(channel, status, email, keyword, isDifficult, orgFlag, beginTime, endTime, pageable);
        helpRecordDTOS.filter(h -> h.getStatus() == 4)
                .forEach(helpRecordDTO -> helpRecordDTO.setDownloadUrl(global.getCloudHost() + "/doc-delivery/file/download/"+helpRecordDTO.getId()));
        return ResponseModel.ok().setBody(helpRecordDTOS);
    }

    @ApiOperation(value = "待应助列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "channel", value = "求助渠道，0:paper平台，1：QQ,2:SPIS,3:智汇云，4：CRS", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "isDifficult", value = "是否是疑难文献", dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "isOrg", value = "只显示本校(默认false,查询所有)", dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "beginTime", value = "起始时间（默认最近一个月）", dataType = "Date", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "Date", paramType = "query")
    })
    @GetMapping("/help/records/wait")
    public ResponseModel helpWaitList(@RequestParam(required = false) List<Long> channel,
                                      @RequestParam(required = false) Boolean isDifficult,
                                      @RequestParam(required = false, defaultValue = "false") boolean isOrg,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                      @PageableDefault(sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable) {
        JSONObject org = (JSONObject) request.getSession().getAttribute(SessionConstant.ORG);
        String orgFlag = org != null && isOrg ? org.getStr("flag") : null;
        endTime = endTime == null ? new Date() : endTime;
        beginTime = beginTime == null ? DateUtil.offsetMonth(endTime, -1).toJdkDate() : beginTime;
        Page<HelpRecordDTO> waitHelpRecords = frontService.getWaitHelpRecords(channel, isDifficult, orgFlag, beginTime, endTime, pageable);
        return ResponseModel.ok().setBody(waitHelpRecords);
    }


    @ApiOperation(value = "求助成功列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "channel", value = "求助渠道，0:paper平台，1：QQ,2:SPIS,3:智汇云，4：CRS", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "isOrg", value = "只显示本校(默认false,查询所有)", dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "beginTime", value = "起始时间（默认最近一周）", dataType = "Date", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "Date", paramType = "query")
    })
    @GetMapping("/help/records/success")
    public ResponseModel helpSuccessList(@RequestParam(required = false) List<Long> channel,
                                         @RequestParam(required = false, defaultValue = "false") boolean isOrg,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                         @PageableDefault(sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable) {
        JSONObject org = (JSONObject) request.getSession().getAttribute(SessionConstant.ORG);
        String orgFlag = org != null && isOrg ? org.getStr("flag") : null;
        Page<HelpRecordDTO> successHelpRecords = frontService.getSuccessHelpRecords(channel, orgFlag, beginTime, endTime, pageable);
        successHelpRecords.forEach(helpRecordDTO -> helpRecordDTO.setDownloadUrl(global.getCloudHost() + "/doc-delivery/file/download/"+helpRecordDTO.getId()));
        return ResponseModel.ok().setBody(successHelpRecords);
    }

    @ApiOperation(value = "疑难文献列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "channel", value = "求助渠道，0:paper平台，1：QQ,2:SPIS,3:智汇云，4：CRS", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "isOrg", value = "只显示本校(默认false,查询所有)", dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "beginTime", value = "起始时间（默认最近一周）", dataType = "Date", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "Date", paramType = "query")
    })
    @GetMapping("/help/records/failed")
    public ResponseModel helpFailedList(@RequestParam(required = false) List<Long> channel,
                                        @RequestParam(required = false, defaultValue = "false") boolean isOrg,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                        @PageableDefault(sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable) {
        JSONObject org = (JSONObject) request.getSession().getAttribute(SessionConstant.ORG);
        String orgFlag = org != null && isOrg ? org.getStr("flag") : null;
        endTime = endTime == null ? new Date() : endTime;
        beginTime = beginTime == null ? DateUtil.offsetWeek(endTime, -1).toJdkDate() : beginTime;
        Page<HelpRecordDTO> finishHelpRecords = frontService.getDifficultHelpRecords(channel, orgFlag, beginTime, endTime, pageable);

        return ResponseModel.ok().setBody(finishHelpRecords);
    }


    @ApiOperation(value = "我的求助记录")
    @ApiImplicitParam(name = "status", value = "状态", dataType = "List", paramType = "query")
    @ValidateLogin
    @GetMapping("/help/records/my")
    public ResponseModel myHelpRecords(@RequestParam(required = false) List<Integer> status,
                                       @RequestParam(required = false) Boolean isDifficult,
                                       @PageableDefault(sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable) {
        JSONObject loginUser = (JSONObject) request.getSession().getAttribute(SessionConstant.LOGIN_USER);
        String username = loginUser != null ? loginUser.getStr("username") : null;
        Page<HelpRecordDTO> myHelpRecords = frontService.myHelpRecords(username, status, isDifficult, pageable);
        myHelpRecords.filter(h -> h.getStatus() == 4)
                .forEach(helpRecordDTO -> helpRecordDTO.setDownloadUrl(global.getCloudHost() + "/doc-delivery/file/download/"+helpRecordDTO.getId()));
        return ResponseModel.ok().setBody(myHelpRecords);
    }

    @ApiOperation(value = "我的应助记录")
    @ApiImplicitParam(name = "status", value = "状态", dataType = "List", paramType = "query")
    @ValidateLogin
    @GetMapping("/give/records/my")
    public ResponseModel myGiveRecords(@RequestParam(required = false) List<Integer> status,
                                       @PageableDefault(sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable) {
        JSONObject loginUser = (JSONObject) request.getSession().getAttribute(SessionConstant.LOGIN_USER);
        String username = loginUser != null ? loginUser.getStr("username") : null;
        Page<GiveRecordDTO> myGiveRecords = frontService.myGiveRecords(username, status, pageable);
        return ResponseModel.ok().setBody(myGiveRecords);
    }


    @ApiOperation(value = "应助认领")
    @ApiImplicitParam(name = "helpRecordId", value = "求助记录ID", dataType = "Long", paramType = "path")
    @ValidateLogin
    @PatchMapping("/help/records/{helpRecordId}/giving")
    public ResponseModel giving(@PathVariable Long helpRecordId) {
        String ip = ServletUtil.getClientIP(request);
        JSONObject loginUser = (JSONObject) request.getSession().getAttribute(SessionConstant.LOGIN_USER);
        String username = loginUser != null ? loginUser.getStr("username") : null;
        frontService.give(helpRecordId, username, ip);
        return ResponseModel.ok().setMessage("应助认领成功");
    }


    @ApiOperation(value = "应助取消")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "helpRecordId", value = "求助记录ID", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "username", value = "应助者用户名称", dataType = "String", paramType = "query")
    })
    @ValidateLogin
    @PatchMapping("/help/records/{helpRecordId}/giving/cancel")
    public ResponseModel cancelGiving(@PathVariable Long helpRecordId) {
        JSONObject loginUser = (JSONObject) request.getSession().getAttribute(SessionConstant.LOGIN_USER);
        String username = loginUser != null ? loginUser.getStr("username") : null;
        frontService.cancelGivingHelp(helpRecordId, username);
        return ResponseModel.ok();
    }


    @ApiOperation(value = "应助上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "helpRecordId", value = "求助记录ID", dataType = "Long", paramType = "path")
    })
    @ValidateLogin
    @PostMapping("/give/upload/{helpRecordId}")
    public ResponseModel upload(@PathVariable Long helpRecordId,
                                @NotNull MultipartFile file) {
        JSONObject loginUser = (JSONObject) request.getSession().getAttribute(SessionConstant.LOGIN_USER);
        String username = loginUser != null ? loginUser.getStr("username") : null;
        String ip = ServletUtil.getClientIP(request);
        if (file == null) {
            return ResponseModel.fail(StatusEnum.DOC_FILE_EMPTY);
        } else if (!global.getFileTypes().contains(StrUtil.subAfter(file.getOriginalFilename(), ".", true))) {
            return ResponseModel.fail(StatusEnum.DOC_FILE_TYPE_ERROR);
        }
        // 检查求助记录状态是否为HelpStatusEnum.HELPING
        HelpRecord helpRecord = frontService.getHelpingRecord(helpRecordId);
        if (helpRecord == null) {
            return ResponseModel.fail(StatusEnum.DOC_HELP_NOT_FOUND);
        }
        frontService.uploadFile(helpRecord, username, file, ip);
        return ResponseModel.ok().setMessage("应助成功，感谢您的帮助");
    }


    @ApiOperation(value = "查询邮箱当天已求助记录的数量")
    @ApiImplicitParam(name = "email", value = "用户邮箱", dataType = "String", paramType = "query")
    @GetMapping("/help/count")
    public ResponseModel getUserHelpCountToDay(@RequestParam String email) {

        return ResponseModel.ok().setBody(frontService.getCountHelpRecordToDay(email));
    }

    @ApiOperation(value = "下一个级别的求助上限")
    @GetMapping("/level/next")
    public ResponseModel nextLevel() {
        Integer level = (Integer) request.getSession().getAttribute(SessionConstant.LEVEL);
        JSONObject org = (JSONObject) request.getSession().getAttribute(SessionConstant.ORG);
        String orgFlag = org != null ? org.getStr("flag") : null;
        Permission permission = frontService.nextPermission(orgFlag, level);
        Map<String, Long> resp = new HashMap<>();
        if (permission == null) {
            resp.put("todayTotal", 10L);
            resp.put("total", 20L);
        } else {
            resp.put("todayTotal", permission.getTodayTotal());
            resp.put("total", permission.getTotal());
        }
        return ResponseModel.ok().setBody(resp);
    }

    @ApiOperation(value = "查询当前邮箱15天内是否求助该文章")
    @GetMapping("/help/repeat")
    public ResponseModel addHelpRecordRepeat(String docTitle, String docHref, String helperEmail) {
        try {
            log.info("查询重复的title" + docTitle);
            helpRequestService.checkIsRepeat(HtmlUtil.unescape(HtmlUtil.cleanHtmlTag(docTitle.trim())), docHref, helperEmail);
            return ResponseModel.ok().setMessage("15天内没有求助过当前文章");
        } catch (ConstraintViolationException e) {
            throw new AppException(ExceptionEnum.HELP_REPEAT);
        }
    }

}
