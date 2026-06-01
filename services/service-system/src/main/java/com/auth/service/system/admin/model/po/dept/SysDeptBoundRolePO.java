package com.auth.service.system.admin.model.po.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门详情-已授权角色行
 */
@Schema(name = "SysDeptBoundRolePO", title = "部门已授权角色 PO")
@Getter
@Setter
public class SysDeptBoundRolePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态(true=正常启用，false=停用)")
	private Boolean status;

	@Schema(title = "角色类型(1=系统,2=菜单,3=业务,4=自定义)")
	private Integer roleType;

	@Schema(title = "授权时间")
	private LocalDateTime grantTime;

}
