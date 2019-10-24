package com.wd.cloud.docdelivery.exception;

/**
 * @author He Zhigang
 * @date 2019/1/19
 * @Description:
 */
public enum ExceptionEnum {

    /**
     * 总求助上限
     */
    HELP_TOTAL_CEILING(1001201, "求助已达上限"),
    HELP_TOTAL_TODAY_CEILING(1001202, "今日求助已达上限"),
    GIVE_ING(1001203, "该求助正在被其它人应助"),
    GIVE_CLAIM(1001204, "请完成已认领的求助"),
    FLOW_STATUS(1001205, "流程状态违法"),
    HELP_REPEAT(1001206, "您最近15天内已求助过这篇文献,请注意查收邮箱"),
    HELP_PARAM(1001207, "邮箱或标题不能为空");
    private String message;
    private Integer status;

    private ExceptionEnum(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public String message() { return message;
    }

    public Integer status() { return status;
    }
}
