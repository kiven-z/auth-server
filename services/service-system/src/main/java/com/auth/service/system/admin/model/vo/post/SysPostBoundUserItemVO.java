package com.auth.service.system.admin.model.vo.post;

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
 * 岗位详情-关联用户项
 *
 * @author Bunny
 */
@Schema(name = "SysPostBoundUserItemVO", title = "岗位关联用户项")
@Getter
@Setter
@ToString
public class SysPostBoundUserItemVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
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

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "授权时间")
	private LocalDateTime grantTime;

}
