package com.wd.cloud.docdelivery.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author He Zhigang
 * @date 2018/12/11
 * @Description: 统计分析返回对象
 */
@Data
@Accessors(chain = true)
public class AnalysisModel {

    private String orgName;
    private String orgFlag;
    /**
     * 求助总数量
     */
    private int sumCount;
    /**
     * 求助人数
     */
    private int helperCount;
    /**
     * 成功数量
     */
    private int successCount;
    /**
     * 失败数量
     */
    private int failedCount;
    /**
     * 待应助，待审核，待上传，求助第三方都算otherCount
     */
    private int otherCount;


}
