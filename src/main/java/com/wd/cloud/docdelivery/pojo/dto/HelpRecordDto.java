package com.wd.cloud.docdelivery.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author He Zhigang
 * @date 2018/12/26
 * @Description:
 */
@Data
@Accessors(chain = true)
public class HelpRecordDto {

    private Long id;

    private Date gmtCreate;

    private Date gmtModified;

    private String docTitle;

    private String docHref;

    private String fileId;

    private String helperEmail;

    private String helperName;

    private String orgName;

    private String helperIp;

    private Integer helpChannel;
    /**
     * 互助状态
     * 0：待应助，
     * 1：应助中（用户已认领，15分钟内上传文件），
     * 2: 待审核（用户已应助），
     * 3：求助第三方（第三方应助），
     * 4：应助成功（审核通过或管理员应助），
     * 5：应助失败（超过15天无结果）
     */
    private Integer status;

    private Boolean difficult;

    private String remark;

    private String handlerName;

    private String giverName;

    private Integer giveType;

    private String watchName;

    private String downloadUrl;
}
