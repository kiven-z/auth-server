package com.auth.service.system.common.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户档案聚合（基本信息 + 部门 + 岗位）
 *
 * @author Bunny
 */
@Schema(name = "SysUserProfileResponse", title = "用户档案")
@Getter
@Setter
public class SysUserProfileResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long id;

	@Schema(title = "登录账号")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "邮箱")
	private String email;

	@Schema(title = "手机号")
	private String phone;

	@Schema(title = "头像URL")
	private String avatar;

	@Schema(title = "状态(0=禁用,1=正常,2=锁定)")
	private Integer status;

	@Schema(title = "性别(0=未知,1=男,2=女)")
	private Integer gender;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Schema(title = "生日")
	private LocalDate birthday;

	@Schema(title = "个人简介")
	private String introduction;

	@Schema(title = "所属部门列表")
	private List<SysUserDeptProfileItemResponse> deptList = new ArrayList<>();

	@Schema(title = "岗位列表")
	private List<SysUserPostProfileItemResponse> postList = new ArrayList<>();

}
