package com.wxl.agent.model.dto.enums;

public enum UserRoleEnum {
    USER("user", "普通用户"),
    ADMIN("admin", "管理员"),
    VIP("vip", "VIP用户"); // 以后你做商业化就可以直接用这个

    private final String value;
    private final String text;

    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }
}