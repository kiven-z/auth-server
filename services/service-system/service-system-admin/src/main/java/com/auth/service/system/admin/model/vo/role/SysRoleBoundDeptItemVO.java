package com.auth.service.system.admin.model.vo.role;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色详情-已授权部门项
 *
 * @author Bunny
 */
@Schema(name = "SysRoleBoundDeptItemVO", title = "角色已授权部门项")
@Getter
@Setter
@ToString
public class SysRoleBoundDeptItemVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "部门ID")
	private Long deptId;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "启用状态(true=正常，false=禁用)")
	private Boolean status;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

}
