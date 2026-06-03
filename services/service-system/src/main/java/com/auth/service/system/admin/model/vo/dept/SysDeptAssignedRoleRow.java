package com.auth.service.system.admin.model.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门已分配角色行
 *
 * @author bunny
 */
@Schema(name = "SysDeptAssignedRoleRow", title = "部门已分配角色行")
@Getter
@Setter
public class SysDeptAssignedRoleRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

}
