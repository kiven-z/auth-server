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
	ROLE("角色"),;

	private final String description;

	DataScopeGrantTableSubjectType(String description) {
		this.description = description;
	}

	/**
	 * 查询所有授权主体
	 * @return 授权主体列表
	 */
	public static List<GrantSubject> grantSubjectList() {
		return Arrays.stream(DataScopeGrantTableSubjectType.values()).map(grantTableSubjectType -> {
			GrantSubject grantSubject = new GrantSubject();
			grantSubject.setSubject(grantTableSubjectType.name());
			grantSubject.setDescription(grantTableSubjectType.getDescription());
			return grantSubject;
		}).toList();
	}

}