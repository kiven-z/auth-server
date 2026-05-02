package com.auth.service.auth.model.projection;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户角色与权限聚合投影
 *
 * <p>
 * 用于 MyBatis {@code resultMap} + {@code collection} 折叠多行结果，将角色/权限编码分别收集到列表。
 * </p>
 *
 * @author Bunny
 */
@Getter
@Setter
public class RolePermissionProjection implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 折叠主键（仅用于 MyBatis 将多行结果归并为同一个对象）
	 */
	private Long profileId;

	/**
	 * 角色编码列表
	 */
	private List<String> roles = new ArrayList<>();

	/**
	 * 权限编码列表
	 */
	private List<String> permissions = new ArrayList<>();

}
