package com.auth.service.system.admin.user.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户档案中的部门关联行
 *
 * @author Bunny
 */
@Data
@Schema(name = "SysUserDeptProfileItemResponse", title = "用户档案-部门项")
public class SysUserDeptProfileItemResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "部门ID")
	@JsonStringFormat
	private Long deptId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

	@Schema(title = "关联状态(0=正常,1=已调离,2=兼职)")
	private Integer status;

	@Schema(title = "在部门内职务")
	private String position;

	@Schema(title = "加入部门时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime joinTime;

}
