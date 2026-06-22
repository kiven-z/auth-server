package com.auth.service.system.admin.model.po.granttable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * grant_table 主体已授权角色查询投影
 *
 * @author Bunny
 */
@Schema(name = "GrantTableBoundRolePO", title = "grant_table 已授权角色 PO")
@Getter
@Setter
public class GrantTableBoundRolePO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色 ID")
	private Long roleId;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态(true=正常启用，false=停用)")
	private Boolean status;

	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

}
