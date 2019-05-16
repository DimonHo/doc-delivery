package com.wd.cloud.docdelivery.enums;

/**
 * @author He Zhigang
 * @date 2019/1/5
 * @Description:
 */
public enum GiveStatusEnum {

    /**
     * 待上传
     */
    WAIT_UPLOAD("待上传", 0),
    WAIT_AUDIT("待审核", 1),
    THIRD("求助第三方", 2),
    CANCEL("已取消", 3),
    TIME_OUT("已超时", 4),
    AUDIT_NO_PASS("审核不通过", 5),
    SUCCESS("审核通过或管理员应助成功", 6),
    NO_RESULT("无结果", 7);

    private String name;
    private int value;

    private GiveStatusEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public static String name(int value) {
        for (GiveStatusEnum giveStatusEnum : GiveStatusEnum.values()) {
            if (giveStatusEnum.value() == value) {
                return giveStatusEnum.name();
            }
        }
        return null;
    }

    public static GiveStatusEnum match(int value) {
        for (GiveStatusEnum giveStatusEnum : GiveStatusEnum.values()) {
            if (giveStatusEnum.value() == value) {
                return giveStatusEnum;
            }
        }
        return null;
    }


    public Integer value() {
        return value;
    }

}
