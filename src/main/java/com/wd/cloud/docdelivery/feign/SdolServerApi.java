package com.wd.cloud.docdelivery.feign;

import com.wd.cloud.commons.model.ResponseModel;
import com.wd.cloud.docdelivery.pojo.entity.Literature;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author He Zhigang
 * @date 2019/1/17
 * @Description:
 */
@FeignClient(value = "sdol-server", url = "${feign.url.sdol-server}")
public interface SdolServerApi {

    @PostMapping("/searchpdf")
    ResponseModel<String> search(@RequestBody Literature literature);

    @GetMapping("/search/{rowkey}")
    ResponseModel<byte[]> getFileByte(@PathVariable(value = "rowkey") String rowkey);

}
