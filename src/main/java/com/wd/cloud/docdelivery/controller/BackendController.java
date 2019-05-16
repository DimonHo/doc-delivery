package com.wd.cloud.docdelivery.controller;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.config.Global;
import com.wd.cloud.docdelivery.pojo.dto.HelpRecordDTO;
import com.wd.cloud.docdelivery.service.BackendService;
import com.wd.cloud.docdelivery.service.FileService;
import com.wd.cloud.docdelivery.service.MailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
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
    Global global;


    /**
     * 文献互助列表
     *
     * @return
     */
    @ApiOperation(value = "文献互助列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "orgFlag", value = "学校falg", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "beginTime", value = "开始时间", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String", paramType = "query")
    })
    @GetMapping("/helpRecords/view")
    public ResponseModel helpList(@RequestParam(required = false) Integer status,
                                  @RequestParam(required = false) String orgFlag,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String beginTime,
                                  @RequestParam(required = false) String endTime,
                                  @PageableDefault(value = 20, sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("orgFlag", orgFlag);
        param.put("status", status);
        param.put("keyword", keyword);
        param.put("beginTime", beginTime);
        param.put("endTime", endTime);
        Page<HelpRecordDTO> helpRecordDTOPage = backendService.getHelpList(pageable, param);
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
        Map<String, Object> param = new HashMap<String, Object>();
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
    public ResponseModel getDocFileList(@RequestParam Long literatureId,
                                        @PageableDefault(sort = {"gmtCreate"}, direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseModel.ok().setBody(backendService.getDocFileList(pageable, literatureId));
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
        backendService.failed(id, username);
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
        backendService.auditPass(id, username);
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
        backendService.auditNoPass(id, username);
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


}
