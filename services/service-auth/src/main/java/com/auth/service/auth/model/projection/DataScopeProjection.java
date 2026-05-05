package com.auth.service.auth.model.projection;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 数据权限行数据
 *
 * @author Bunny
 */
@Getter
@Setter
public class DataScopeProjection implements Serializable {

	// TODO 需要和 AuthProfile 对齐
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 来源优先级：1=用户覆盖，2=角色兜底
	 */
	private Integer sourceLevel;

	/**
	 * 范围类型：SELF/DEPT/DEPT_AND_CHILD/ALL/CUSTOM_POLICY
	 */
	private String scopeType;

	/**
	 * 部门范围值（JSON 数组）
	 */
	private String scopeDeptIds;

	/**
	 * 自定义策略编码
	 */
	private String policyCode;

}
