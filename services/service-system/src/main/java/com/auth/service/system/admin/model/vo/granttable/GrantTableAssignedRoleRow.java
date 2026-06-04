package com.auth.service.system.admin.model.vo.granttable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * grant_table 主体已分配角色列表行（抽屉/管理端查询）
 *
 * @author Bunny
 */
@Schema(name = "GrantTableAssignedRoleRow", title = "grant_table 已分配角色行")
@Getter
@Setter
public class GrantTableAssignedRoleRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

}
