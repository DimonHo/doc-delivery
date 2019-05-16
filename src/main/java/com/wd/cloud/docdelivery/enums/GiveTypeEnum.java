package com.wd.cloud.docdelivery.enums;

/**
 * @author He Zhigang
 * @date 2018/5/22
 * @Description:
 */
public enum GiveTypeEnum {

    /**
     * 应助者类型
     */
    AUTO("自动应助", 0),
    MANAGER("管理员应助", 1),
    USER("用户应助", 2),
    BIG_DB("数据库全文", 3);

    private String name;
    private int value;

    private GiveTypeEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int value() {
        return value;
    }

}
