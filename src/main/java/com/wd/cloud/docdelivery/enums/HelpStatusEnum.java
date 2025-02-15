package com.wd.cloud.docdelivery.enums;

import java.util.Optional;

/**
 * @author He Zhigang
 * @date 2018/5/7
 * @Description: 互助状态
 */
public enum HelpStatusEnum {

    /**
     * 待应助
     */
    WAIT_HELP("待应助", 0),
    /**
     * 应助中
     */
    HELPING("应助中", 1),
    /**
     * 待审核
     */
    WAIT_AUDIT("待审核", 2),
    /**
     * 求助第三方
     */
    HELP_THIRD("求助第三方", 3),
    /**
     * 审核通过或后台处理
     */
    HELP_SUCCESSED("应助成功", 4),
    /**
     * 准成功，5分钟后自动转为成功状态，准成功状态无法被查询
     */
    HELP_SUCCESSING("准成功", -1);


    private String name;
    private int value;

    private HelpStatusEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public static Optional<HelpStatusEnum> match(int value) {
        for (HelpStatusEnum helpStatusEnum : HelpStatusEnum.values()) {
            if (helpStatusEnum.value() == value) {
                return Optional.of(helpStatusEnum);
            }
        }
        return Optional.empty();
    }


    public int value() {
        return value;
    }

}
