package com.auth.service.system.admin.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色类型字典
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum RoleTypeEnum {

    /**
     * 系统角色
     */
    SYSTEM(1, "系统角色"),

    /**
     * 业务角色
     */
    BUSINESS(2, "业务角色"),

    /**
     * 自定义角色
     */
    CUSTOM(3, "自定义角色"),
    ;

    private final Integer code;

    private final String desc;

    public static String getDesc(Integer code) {
        for (RoleTypeEnum value : RoleTypeEnum.values()) {
            if (value.code.equals(code)) {
                return value.desc;
            }
        }
        return null;
    }

}
