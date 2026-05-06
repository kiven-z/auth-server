package com.auth.module.security.contract.api.granttable;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 数据范围授权主体类型
 *
 * @author Bunny
 */
@Getter
public enum DataScopeGrantTableSubjectType {

    /**
     * 用户
     */
    USER("用户"),

    /**
     * 部门
     */
    DEPT("部门"),

    /**
     * 岗位
     */
    POST("岗位"),

    /**
     * 角色
     */
    ROLE("角色");

    private final String description;

    DataScopeGrantTableSubjectType(String description) {
        this.description = description;
    }

    /**
     * 查询所有授权主体
     *
     * @return 授权主体列表
     */
    public static List<GrantSubjectOption> grantSubjectList() {
        return Arrays.stream(DataScopeGrantTableSubjectType.values()).map(grantTableSubjectType -> {
            GrantSubjectOption grantSubjectOption = new GrantSubjectOption();
            grantSubjectOption.setSubject(grantTableSubjectType.name());
            grantSubjectOption.setDescription(grantTableSubjectType.getDescription());
            return grantSubjectOption;
        }).toList();
    }

}