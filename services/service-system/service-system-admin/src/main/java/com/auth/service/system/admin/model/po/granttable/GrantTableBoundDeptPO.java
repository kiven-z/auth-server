package com.auth.service.system.admin.model.po.granttable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * grant_table 按角色反查已授权部门投影
 *
 * @author Bunny
 */
@Schema(name = "GrantTableBoundDeptPO", title = "grant_table 已授权部门 PO")
@Getter
@Setter
public class GrantTableBoundDeptPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "部门ID")
	private Long deptId;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "启用状态(true=正常，false=禁用)")
	private Boolean status;

	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

}
