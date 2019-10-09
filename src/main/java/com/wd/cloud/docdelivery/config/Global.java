package com.wd.cloud.docdelivery.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author He Zhigang
 * @date 2018/5/3
 */
@Data
@Accessors(chain = true)
@RefreshScope
@Component
@ConfigurationProperties(value = "global")
public class Global {

    private String cloudHost;

    /**
     * 文件在hbase的位置
     */
    private String hbaseTableName;

    /**
     * 上传文件类型
     */
    private List<String> fileTypes;

    /**
     * 邮件业务类型，与mail-server中的business对应
     */
    private String bizSuccess;

    private String bizOther;

}
