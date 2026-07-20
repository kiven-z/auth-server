package com.auth.service.system.admin.model.po.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户分页行
 *
 * @author Bunny
 */
@Schema(name = "SysUserPageRowPO", title = "用户分页行（PO）")
@Getter
@Setter
@ToString
public class SysUserPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "头像 URL")
	private String avatar;

	@Schema(title = "手机号")
	private String phone;

	@Schema(title = "邮箱")
	private String email;

	@Schema(title = "工号")
	private String employeeNo;

	@Schema(title = "账号状态(0=禁用,1=正常,2=锁定)")
	private Integer status;

	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@Schema(title = "创建用户")
	private Long createUser;

	@Schema(title = "更新用户")
	private Long updateUser;

}
