package com.auth.service.system.admin.model.vo.dept;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门详情-关联用户项
 */
@Schema(name = "SysDeptBoundUserItemVO", title = "部门关联用户项")
@Getter
@Setter
public class SysDeptBoundUserItemVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户ID")
	@JsonStringFormat
	private Long userId;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "工号")
	private String employeeNo;

	@Schema(title = "用户状态(0=禁用,1=正常,2=锁定)")
	private Integer userStatus;

	@Schema(title = "是否主部门")
	private Boolean isPrimary;

	@Schema(title = "用户-部门关联状态(0=已调离,1=正常,2=兼职)")
	private Integer relationStatus;

	@Schema(title = "部门内职位")
	private String position;

	@Schema(title = "加入部门时间")
	private LocalDateTime joinTime;

}
