package com.wd.cloud.docdelivery.controller;

import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.service.TempService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author He Zhigang
 * @date 2019/1/2
 * @Description:
 */
@RestController
public class TempController {

    @Autowired
    TempService tempService;

    @GetMapping("/temp/up-unid")
    public ResponseModel updateLiteratureUnid() {
        int upCount = tempService.updateLiteratureUnid();
        return ResponseModel.ok().setBody(upCount);
    }


    @GetMapping("/deleteLiteratureUnid")
    public ResponseModel deleteLiteratureUnid() {
        tempService.deleteLiteratureUnid();
        return ResponseModel.ok();
    }

    @GetMapping("/updateHandlerName")
    public ResponseModel updateHandlerName() {
        tempService.updateHandlerName();
        return ResponseModel.ok();
    }
}
