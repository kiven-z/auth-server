package com.auth.service.system.admin.model.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位已分配角色行 FIXME 这个是否和很多都是一样的？菜单、部门、岗位？
 *
 * @author Bunny
 */
@Schema(name = "SysPostAssignedRoleRow", title = "岗位已分配角色行")
@Getter
@Setter
public class SysPostAssignedRoleRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

}
