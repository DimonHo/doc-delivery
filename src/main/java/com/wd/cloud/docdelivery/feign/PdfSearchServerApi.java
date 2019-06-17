package com.wd.cloud.docdelivery.feign;

import com.wd.cloud.commons.enums.StatusEnum;
import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.pojo.entity.Literature;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author He Zhigang
 * @date 2019/1/17
 * @Description:
 */
@FeignClient(value = "pdfsearch-server", url = "${feign.url.pdfsearch-server}", fallback = PdfSearchServerApi.Fallback.class)
public interface PdfSearchServerApi {

    @PostMapping("/searchpdf")
    public ResponseModel<String> search(@RequestBody Literature literature);

    @GetMapping("/search/{rowkey}")
    public ResponseModel<byte[]> getFileByte(@PathVariable(value = "rowkey") String rowkey);

    @Component("pdfSearchServerApi")
    class Fallback implements PdfSearchServerApi {


        @Override
        public ResponseModel<String> search(Literature literature) {
            return ResponseModel.fail(StatusEnum.FALL_BACK);
        }

        @Override
        public ResponseModel<byte[]> getFileByte(String fileId) {
            return ResponseModel.fail(StatusEnum.FALL_BACK);
        }
    }
}
