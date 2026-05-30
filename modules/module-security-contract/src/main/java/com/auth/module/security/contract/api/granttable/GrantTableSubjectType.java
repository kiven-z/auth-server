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
	public static List<GrantSubjectOption> grantSubjectList() {
		return Arrays.stream(GrantTableSubjectType.values()).map(grantTableSubjectType -> {
			GrantSubjectOption grantSubjectOption = new GrantSubjectOption();
			grantSubjectOption.setSubject(grantTableSubjectType.name());
			grantSubjectOption.setDescription(grantTableSubjectType.getDescription());
			return grantSubjectOption;
		}).toList();
	}

}
