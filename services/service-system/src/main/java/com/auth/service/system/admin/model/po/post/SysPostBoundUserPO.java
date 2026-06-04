package com.auth.service.system.admin.model.po.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位详情-关联用户行
 *
 * @author Bunny
 */
@Schema(name = "SysPostBoundUserPO", title = "岗位已关联用户 PO")
@Getter
@Setter
public class SysPostBoundUserPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "工号")
	private String employeeNo;

	@Schema(title = "用户状态(0=禁用,1=正常,2=锁定)")
	private Integer userStatus;

	@Schema(title = "是否主岗位")
	private Boolean isPrimary;

	@Schema(title = "用户-岗位关联状态(0=已调离,1=正常,2=兼职)")
	private Integer relationStatus;

	@Schema(title = "授权时间")
	private LocalDateTime grantTime;

}
