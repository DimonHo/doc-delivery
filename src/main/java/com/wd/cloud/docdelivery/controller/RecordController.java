package com.wd.cloud.docdelivery.controller;

import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.config.Global;
import com.wd.cloud.docdelivery.model.AnalysisModel;
import com.wd.cloud.docdelivery.repository.DocFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author He Zhigang
 * @date 2018/5/22
 * @Description: 文献传递相关报表
 */
@RestController
@RequestMapping("/record")
public class RecordController {

    @Autowired
    Global global;
    @Autowired
    DocFileRepository docFileRepository;

    @GetMapping("/analysis")
    public ResponseModel<AnalysisModel> analysis(@RequestParam(required = false) String query,
                                                 @RequestParam String beginDate,
                                                 @RequestParam String endDate) {

        return ResponseModel.ok();
    }

    /**
     * 审核不通过记录报表
     *
     * @return
     */
    @GetMapping("/process/audit/fail/list")
    public ResponseModel auditFails() {
        return ResponseModel.ok();
    }

    /**
     * 应助失败记录报表
     *
     * @return
     */
    @GetMapping("/give/fail/list")
    public ResponseModel giveFails() {
        return ResponseModel.ok();
    }

    /**
     * 应助成功记录报表
     *
     * @return
     */
    @GetMapping("/give/success/list")
    public ResponseModel giveSuccess() {
        return ResponseModel.ok();
    }

    /**
     * 自动应助的报表
     *
     * @return
     */
    @GetMapping("/give/auto/list")
    public ResponseModel autoGives() {
        return ResponseModel.ok();
    }


}
