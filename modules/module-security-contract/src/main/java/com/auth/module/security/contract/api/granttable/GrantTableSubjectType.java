package com.auth.module.security.contract.api.granttable;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 统一授权主体类型
 *
 * @author Bunny
 */
@Getter
public enum GrantTableSubjectType {

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
	POST("岗位"),;

	private final String description;

	GrantTableSubjectType(String description) {
		this.description = description;
	}

	/**
	 * 查询所有授权主体
	 * @return 授权主体列表
	 */
	public static List<GrantSubject> grantSubjectList() {
		return Arrays.stream(GrantTableSubjectType.values()).map(grantTableSubjectType -> {
			GrantSubject grantSubject = new GrantSubject();
			grantSubject.setSubject(grantTableSubjectType.name());
			grantSubject.setDescription(grantTableSubjectType.getDescription());
			return grantSubject;
		}).toList();
	}

}
