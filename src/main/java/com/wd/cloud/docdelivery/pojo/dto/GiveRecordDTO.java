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
public class GiveRecordDTO {

    Date gmtModified;
    Date gmtCreate;
    private Long helpRecordId;
    /**
     * 应助者名称
     */
    private String giverName;
    /**
     * 应助者IP
     */
    private String giverIp;
    /**
     * 应助者类型：
     * 0：系统自动应助，
     * 1：管理员应助
     * 2：
     * 3：第三方应助,
     * 4:其它
     */
    private Integer type;
    /**
     * 0：待审核，1：审核通过，2用户应助，：审核不通过，4：待上传
     */
    private Integer status;
    /**
     * 审核人
     */
    private String handlerName;
    /**
     * 审核失败原因
     */
    private String auditMsg;
    /**
     * 求助者邮箱
     */
    private String helperEmail;
    /**
     * 文献标题
     */
    private String docTitle;
    private String docHref;
    private String remark;
    private String orgName;
}
