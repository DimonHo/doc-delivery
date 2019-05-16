package com.wd.cloud.docdelivery.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: He Zhigang
 * @Date: 2019/4/29 18:42
 * @Description:
 */
@Data
@Accessors(chain = true)
public class MailMessage {

    private String tos;
    private String ccs;
    private String bccs;
    private String title;
    private String content;
    private Boolean html;

}
