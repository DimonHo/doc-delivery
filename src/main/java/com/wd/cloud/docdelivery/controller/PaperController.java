package com.wd.cloud.docdelivery.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.constant.SessionConstant;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRecord;
import com.wd.cloud.docdelivery.repository.LiteratureRepository;
import com.wd.cloud.docdelivery.repository.VHelpRecordRepository;
import com.wd.cloud.docdelivery.util.BizUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @Author: He Zhigang
 * @Date: 2019/10/14 10:48
 * @Description:
 */
@Api(value = "文献互助平台定制接口", tags = {"文献互助平台"})
@RestController
@RequestMapping("/paper")
public class PaperController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    VHelpRecordRepository vHelpRecordRepository;

    @Autowired
    LiteratureRepository literatureRepository;

    @ApiOperation(value = "待应助列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "isOrg", value = "只显示本校(默认false,查询所有)", dataType = "Boolean", paramType = "query"),
    })
    @GetMapping("/help/records/wait")
    public ResponseModel helpWaitList(@RequestParam(required = false, defaultValue = "false") boolean isOrg,
                                      @PageableDefault(sort = {"gmt_create"}, direction = Sort.Direction.DESC) Pageable pageable) {
        JSONObject org = (JSONObject) request.getSession().getAttribute(SessionConstant.ORG);
        String orgFlag = org != null && isOrg ? org.getStr("flag") : null;
        Date end = new Date();
        Date begin1 = DateUtil.offsetMonth(end, -1).toJdkDate();
        Date begin2 = DateUtil.offsetWeek(end, -1).toJdkDate();
        Page<VHelpRecord> waitHelpRecords = null;
        if (orgFlag == null) {
            waitHelpRecords = vHelpRecordRepository.findByWaitHelp(begin1, begin2, end, pageable);
        } else {
            waitHelpRecords = vHelpRecordRepository.findByWaitHelpForOrg(begin1, begin2, end, orgFlag, pageable);
        }
        return ResponseModel.ok().setBody(BizUtil.coversHelpRecordDTO(waitHelpRecords, literatureRepository));
    }

}
