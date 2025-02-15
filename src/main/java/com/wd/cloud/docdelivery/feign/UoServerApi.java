package com.wd.cloud.docdelivery.feign;


import cn.hutool.json.JSONObject;
import com.wd.cloud.commons.enums.StatusEnum;
import com.wd.cloud.commons.model.ResponseModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "uo-server", url = "${feign.url.uo-server}")
public interface UoServerApi {

    @GetMapping("/org")
    ResponseModel<JSONObject> org(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String flag,
                                  @RequestParam(required = false) String ip);

    /**
     * 通过邮箱或用户名获取用户信息
     *
     * @param id
     * @return
     */
    @GetMapping("/user")
    ResponseModel<JSONObject> user(@RequestParam(value = "id") String id);

}
