package com.wd.cloud.docdelivery.controller;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.wd.cloud.docdelivery.config.Global;
import com.wd.cloud.docdelivery.model.DownloadFileModel;
import com.wd.cloud.docdelivery.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author He Zhigang
 * @date 2018/6/12
 * @Description:
 */
@Api(value = "文件controller", tags = {"文件上传下载接口"})
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    FileService fileService;

    @Autowired
    Global global;

    /**
     * 文献下载
     *
     * @return
     */
    @ApiOperation(value = "求助文件下载")
    @ApiImplicitParam(name = "helpRecodeId", value = "求助记录ID", dataType = "Long", paramType = "path")
    @GetMapping("/download/{helpRecodeId}")
    public void download(@PathVariable Long helpRecodeId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        DownloadFileModel downloadFileModel = fileService.getDownloadFile(helpRecodeId);
        if (downloadFileModel == null) {
            response.sendRedirect(global.getCloudDomain() + "/doc-delivery/fileNotFind.html");
            return;
        }
        String filename = null;
        //判断是否是IE浏览器
        if (request.getHeader(Header.USER_AGENT.toString()).toLowerCase().contains("msie")) {
            filename = URLUtil.encode(downloadFileModel.getDownloadFileName(), CharsetUtil.UTF_8);
        } else {
            filename = new String(downloadFileModel.getDownloadFileName().getBytes(CharsetUtil.UTF_8), CharsetUtil.ISO_8859_1);
        }
        String disposition = StrUtil.format("attachment; filename=\"{}\"", filename);
        response.setHeader(Header.CACHE_CONTROL.toString(), "no-cache, no-store, must-revalidate");
        response.setHeader(Header.CONTENT_DISPOSITION.toString(), disposition);
        response.setHeader(Header.PRAGMA.toString(), "no-cache");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        OutputStream out = response.getOutputStream();
        out.write(downloadFileModel.getFileByte());
        out.close();
    }


    /**
     * 文献下载
     *
     * @return
     */
    @ApiOperation(value = "求助文件预览/下载")
    @ApiImplicitParam(name = "helpRecodeId", value = "求助记录ID", dataType = "Long", paramType = "path")
    @GetMapping("/view/{helpRecodeId}")
    public void viewFile(@PathVariable Long helpRecodeId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        DownloadFileModel downloadFileModel = fileService.getWaitAuditFile(helpRecodeId);
        String filename = null;
        //判断是否是IE浏览器
        if (request.getHeader(Header.USER_AGENT.toString()).toLowerCase().contains("msie")) {
            filename = URLUtil.encode(downloadFileModel.getDownloadFileName(), CharsetUtil.UTF_8);
        } else {
            filename = new String(downloadFileModel.getDownloadFileName().getBytes(CharsetUtil.UTF_8), CharsetUtil.ISO_8859_1);
        }
        String disposition = StrUtil.format("attachment; filename=\"{}\"", filename);
        response.setHeader(Header.CACHE_CONTROL.toString(), "no-cache, no-store, must-revalidate");
        response.setHeader(Header.CONTENT_DISPOSITION.toString(), disposition);
        response.setHeader(Header.PRAGMA.toString(), "no-cache");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        OutputStream out = response.getOutputStream();
        out.write(downloadFileModel.getFileByte());
        out.close();
    }

}
