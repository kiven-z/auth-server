package com.auth.service.system.admin.model.po.granttable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * grant_table 按角色反查已授权岗位投影
 *
 * @author Bunny
 */
@Schema(name = "GrantTableBoundPostPO", title = "grant_table 已授权岗位 PO")
@Getter
@Setter
public class GrantTableBoundPostPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "岗位ID")
	private Long postId;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态(true=启用，false=停用)")
	private Boolean status;

	@Schema(title = "所属部门ID")
	private Long deptId;

	@Schema(title = "所属部门名称")
	private String deptName;

	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

}
