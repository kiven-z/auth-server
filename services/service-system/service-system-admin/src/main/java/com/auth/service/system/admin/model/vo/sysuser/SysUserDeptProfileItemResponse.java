package com.auth.service.system.admin.model.vo.sysuser;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户档案中的部门关联行
 *
 * @author Bunny
 */
@Schema(name = "SysUserDeptProfileItemResponse", title = "用户档案-部门项")
@Getter
@Setter
public class SysUserDeptProfileItemResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "部门ID")
	private Long deptId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

	@Schema(title = "关联状态(0=已调离,1=正常,2=兼职)")
	private Integer status;

	@Schema(title = "在部门内职务")
	private String position;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "授权时间")
	private LocalDateTime grantTime;

}
