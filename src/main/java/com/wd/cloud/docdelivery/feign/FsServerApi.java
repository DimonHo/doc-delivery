package com.wd.cloud.docdelivery.feign;

import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.enums.StatusEnum;
import com.wd.cloud.commons.model.ResponseModel;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author He Zhigang
 * @date 2018/11/22
 * @Description:
 */
@FeignClient(value = "fs-server",
        configuration = FsServerApi.MultipartSupportConfig.class,
        fallback = FsServerApi.Fallback.class)
public interface FsServerApi {

    @GetMapping("/check/{dir}/{fileMd5}")
    ResponseModel<JSONObject> checkFile(@PathVariable(value = "dir") String dir,
                                        @PathVariable(value = "fileMd5") String fileMd5);

    @PostMapping(value = "/upload/{dir}", consumes = "multipart/form-data")
    ResponseModel<JSONObject> uploadFile(@PathVariable(value = "dir") String dir,
                                         @RequestPart(value = "file") MultipartFile file);

    @PostMapping(value = "/upload/mulit/{dir}", consumes = "multipart/form-data")
    ResponseModel<JSONObject> uploadFiles(@PathVariable(value = "dir") String dir,
                                          @RequestPart(value = "files") MultipartFile[] files);

    @GetMapping(value = "/download/{unid}")
    ResponseEntity downloadFile(@PathVariable(value = "unid") String unid);

    @GetMapping("/byte/{unid}")
    ResponseModel<byte[]> getFileByte(@PathVariable(value = "unid") String unid);

    @GetMapping("/async")
    ResponseModel hfToUploadRecord(@RequestParam(value = "tableName") String tableName);

    @GetMapping("/getunid")
    ResponseModel<String> getunid(@RequestParam(value = "tableName") String tableName, @RequestParam(value = "fileName") String fileName);


    class MultipartSupportConfig {
        @Autowired
        private ObjectFactory<HttpMessageConverters> messageConverters;

        @Bean
        public Encoder feignFormEncoder() {
            return new SpringFormEncoder();
        }


        @Bean
        public Decoder feignDecoder() {
            final List<HttpMessageConverter<?>> springConverters = messageConverters.getObject().getConverters();
            final List<HttpMessageConverter<?>> decoderConverters
                    = new ArrayList<HttpMessageConverter<?>>(springConverters.size() + 1);

            decoderConverters.addAll(springConverters);
            //decoderConverters.add(new ByteArrayHttpMessageConverter());
            final HttpMessageConverters httpMessageConverters = new HttpMessageConverters(decoderConverters);

            return new SpringDecoder(new ObjectFactory<HttpMessageConverters>() {
                @Override
                public HttpMessageConverters getObject() {
                    return httpMessageConverters;
                }
            });
        }
    }

    @Component("fsServerApi")
    class Fallback implements FsServerApi {

        @Override
        public ResponseModel<JSONObject> checkFile(String dir, String fileMd5) {
            return ResponseModel.fail(StatusEnum.FALL_BACK);
        }

        @Override
        public ResponseModel<JSONObject> uploadFile(String dir, MultipartFile file) {
            return ResponseModel.fail(StatusEnum.FALL_BACK);
        }

        @Override
        public ResponseModel<JSONObject> uploadFiles(String dir, MultipartFile[] files) {
            return ResponseModel.fail(StatusEnum.FALL_BACK);
        }

        @Override
        public ResponseEntity downloadFile(String unid) {
            return ResponseEntity.status(2).build();
        }

        @Override
        public ResponseModel<byte[]> getFileByte(String unid) {
            return ResponseModel.fail(StatusEnum.FALL_BACK);
        }

        @Override
        public ResponseModel hfToUploadRecord(String tableName) {
            return ResponseModel.fail(StatusEnum.FALL_BACK);
        }

        @Override
        public ResponseModel<String> getunid(String tableName, String fileName) {
            return ResponseModel.fail(StatusEnum.FALL_BACK);
        }
    }
}
