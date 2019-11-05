package com.wd.cloud.docdelivery.controller;

import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.config.Global;
import com.wd.cloud.docdelivery.pojo.dto.HelpRawDTO;
import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.pojo.entity.HelpRaw;
import com.wd.cloud.docdelivery.pojo.entity.VHelpRaw;
import com.wd.cloud.docdelivery.pojo.vo.PlanVO;
import com.wd.cloud.docdelivery.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author He Zhigang
 * @date 2018/5/3
 */
@Api(value = "后台controller", tags = {"后台文献处理接口"})
@RestController
@RequestMapping("/backend")
public class BackendController {

    @Autowired
    BackendService backendService;

    @Autowired
    FileService fileService;

    @Autowired
    MailService mailService;

    @Autowired
    LiteraturePlanService literaturePlanService;

    @Autowired
    Global global;

    @Autowired
    HelpRawService helpRawService;


    @ApiOperation(value = "根据ID查询原始求助信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID", dataType = "Long", paramType = "query")
    })
    @GetMapping(value = "/help/raw/findByIdHelpRaw")
    public ResponseModel findByIdHelpRaw(@RequestParam(required = true) Long id){
        List<HelpRaw> helpRaw = helpRawService.findByIdHelpRaw(id);
        return ResponseModel.ok().setBody(helpRaw);
    }

    @ApiOperation(value = "查询原始求助信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "beginTime", value = "开始时间", dataType = "Date", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "Date", paramType = "query"),
            @ApiImplicitParam(name = "anonymous", value = "是否匿名", dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "helpChannel", value = "渠道1：QQ，2：SPIS，3：ZHY，4：CRS，5：PAPER，6：CRS_V2，7：MINI", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "helperEmail", value = "求助者邮箱", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "helperIp", value = "求助者IP", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "helperName", value = "求助者用户名", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "orgFlag", value = "求助者学校ID", dataType = "String" , paramType = "query"),
            @ApiImplicitParam(name = "helpRecordId", value = "求助记录的ID", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "invalid", value = "是否有效：0:待处理,1:无效,2:有效", dataType = "Long", paramType = "query")
    })
    @GetMapping(value = "/help/raw/findHelpRaw")
    public ResponseModel findHelpRaw(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                     @RequestParam(required = false) Boolean anonymous,
                                     @RequestParam(required = false) Long helpChannel,
                                     @RequestParam(required = false) String helperEmail,
                                     @RequestParam(required = false) String helperIp,
                                     @RequestParam(required = false) String helperName,
                                     @RequestParam(required = false) String orgFlag,
                                     @RequestParam(required = false) Long helpRecordId,
                                     @RequestParam(required = false) Integer invalid,
                                     @PageableDefault(sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable){
        Page<VHelpRaw> helpRawDTOS = helpRawService.findHelpRaw(beginTime, endTime ,anonymous, helpChannel, helperEmail, helperIp, helperName, orgFlag, helpRecordId, invalid,pageable);
        return ResponseModel.ok().setBody(helpRawDTOS);
    }

    @ApiOperation(value = "根据原始数据ID修改求助记录的ID和有效值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "helpRecordId", value = "求助记录的ID", dataType = "Long", paramType = "query", defaultValue = "0")
    })
    @PatchMapping(value = "/help/raw/updateHelpRecordId/{id}")
    public ResponseModel updateHelpRecordId(@PathVariable Long id,
                                            @RequestParam(required = false) Long helpRecordId){
        helpRawService.updateHelpRecordId(id,helpRecordId);
        return ResponseModel.ok().setMessage("修改成功");
    }

    /**
     * 文献互助列表
     *
     * @return
     */
    @ApiOperation(value = "文献互助列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "isDifficult", value = "是否是疑难文献", dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "orgFlag", value = "学校falg", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "watchName", value = "值班人员", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "beginTime", value = "开始时间", dataType = "Date", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "Date", paramType = "query")
    })
    @GetMapping("/helpRecords/view")
    public ResponseModel helpList(@RequestParam(required = false) List<Integer> status,
                                  @RequestParam(required = false) Boolean isDifficult,
                                  @RequestParam(required = false) String orgFlag,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String watchName,
                                  @RequestParam(required = false) List<Integer> giveType,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                  @PageableDefault(value = 20, sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable) {

        Page<HelpRecordDTO> helpRecordDTOPage = backendService.getHelpList(status, isDifficult, orgFlag, keyword,watchName,giveType,beginTime,endTime,pageable);
        return ResponseModel.ok().setBody(helpRecordDTOPage);
    }


    /**
     * 文档列表(复用)
     *
     * @return
     */
    @ApiOperation(value = "原数据列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "reusing", value = "是否复用", dataType = "Boolean", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", dataType = "String", paramType = "query")
    })
    @GetMapping("/literature/list")
    public ResponseModel literatureList(@RequestParam(required = false) Boolean reusing, @RequestParam(required = false) String keyword,
                                        @PageableDefault(sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("reusing", reusing);
        param.put("keyword", keyword);
        return ResponseModel.ok().setBody(backendService.getLiteratureList(pageable, param));
    }

    /**
     * 文档列表(复用)
     *
     * @return
     */
    @ApiOperation(value = "应助文档列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "literatureId", value = "元数据id", dataType = "Long", paramType = "query")
    })
    @GetMapping("/docFile/list")
    public ResponseModel getDocFileList(@RequestParam Long literatureId) {

        return ResponseModel.ok().setBody(backendService.getDocFileList(literatureId));
    }

    /**
     * 直接处理，上传文件
     *
     * @return
     */
    @ApiOperation(value = "直接处理，上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "helpRecordId", value = "求助数据id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "username", value = "应助者(处理人)username", dataType = "String", paramType = "query")
    })
    @PostMapping("/upload/{helpRecordId}")
    public ResponseModel upload(@PathVariable Long helpRecordId,
                                @RequestParam String username,
                                @NotNull MultipartFile file) {
        backendService.give(helpRecordId, username, file);
        return ResponseModel.ok().setMessage("文件上传成功");
    }

    /**
     * 提交第三方处理
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "提交第三方处理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "求助数据id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "username", value = "应助者(处理人)username", dataType = "String", paramType = "query")
    })
    @PostMapping("/third/{id}")
    public ResponseModel helpThird(@PathVariable Long id, @RequestParam String username) {

        backendService.third(id, username);

        return ResponseModel.ok().setMessage("已提交第三方处理，请耐心等待第三方应助结果");
    }

    /**
     * 无结果，应助失败
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "无结果处理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "求助数据id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "username", value = "应助者(处理人)username", dataType = "String", paramType = "query")
    })
    @PostMapping("/fiaied/{id}")
    public ResponseModel helpFail(@PathVariable Long id, @RequestParam String username) {
        backendService.difficult(id, username);
        return ResponseModel.ok().setMessage("处理成功");
    }

    /**
     * 审核通过
     *
     * @return
     */
    @ApiOperation(value = "审核通过")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "求助数据id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "username", value = "审核者(处理人)username", dataType = "String", paramType = "query")
    })
    @PatchMapping("/audit/pass/{id}")
    public ResponseModel auditPass(@PathVariable Long id, @RequestParam String username) {
        backendService.audit(id, username,true);
        return ResponseModel.ok();
    }

    /**
     * 审核不通过
     *
     * @return
     */
    @ApiOperation(value = "审核不通过")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "求助数据id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "auditorName", value = "审核者(处理人)username", dataType = "String", paramType = "query")
    })
    @PatchMapping("/audit/nopass/{id}")
    public ResponseModel auditNoPass(@PathVariable Long id, @RequestParam String username) {
        backendService.audit(id, username,false);
        return ResponseModel.ok();
    }


    /**
     * 复用
     *
     * @return
     */
    @ApiOperation(value = "复用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "docFileId", value = "上传文档id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "literatureId", value = "元数据id", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "username", value = "复用人(处理人)username", dataType = "String", paramType = "query")
    })
    @GetMapping("/reusing/pass/{docFileId}")
    public ResponseModel reusing(@PathVariable Long docFileId, @RequestParam Long literatureId, @RequestParam String username) {
        try {
            backendService.reusing(literatureId, docFileId, true, username);
            return ResponseModel.ok();
        } catch (Exception e) {
            return ResponseModel.fail().setMessage("复用失败，请重试！");
        }
    }

    /**
     * 取消复用
     *
     * @return
     */
    @ApiOperation(value = "取消复用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "docFileId", value = "上传文档id", dataType = "Long", paramType = "path"),
            @ApiImplicitParam(name = "literatureId", value = "元数据id", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "reuseUserName", value = "复用人(处理人)username", dataType = "String", paramType = "query")
    })
    @GetMapping("/reusing/nopass/{docFileId}")
    public ResponseModel notReusing(@PathVariable Long docFileId, @RequestParam Long literatureId, @RequestParam String username) {
        try {
            backendService.reusing(literatureId, docFileId, false, username);
            return ResponseModel.ok();
        } catch (Exception e) {
            return ResponseModel.fail().setMessage("取消复用失败，请重试！");
        }
    }


    @ApiOperation(value = "添加排班计划")
    @PostMapping("/plan")
    public ResponseModel addPlan(@RequestBody List<PlanVO> PlanVOs) {
        literaturePlanService.addPlan(PlanVOs);
        return ResponseModel.ok().setMessage("添加成功");
    }

    @DeleteMapping("/plan/{id}")
    public ResponseModel delPlan(@PathVariable Long id){
        literaturePlanService.delPlan(id);
        return ResponseModel.ok().setMessage("删除成功");
    }
}
