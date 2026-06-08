package com.auth.service.system.admin.model.form.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户新增表单
 *
 * @author Bunny
 */
@Schema(name = "SysUserCreateForm", title = "用户新增表单")
@Getter
@Setter
public class SysUserForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 64, message = "用户名长度不能超过64个字符")
	@NotBlank(message = "用户名不能为空")
	private String username;

	@Schema(title = "昵称", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "昵称不能为空")
	@Size(max = 64, message = "昵称长度不能超过64个字符")
	private String nickname;

	@Schema(title = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "邮箱不能为空")
	@Email(message = "邮箱格式不正确")
	@Size(max = 128, message = "邮箱长度不能超过128个字符")
	private String email;

	@Schema(title = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "手机号不能为空")
	@Size(max = 32, message = "手机号长度不能超过32个字符")
	private String phone;

	@Schema(title = "工号")
	@Size(max = 64, message = "工号长度不能超过64个字符")
	private String employeeNo;

	@Schema(title = "账号状态（0=禁用，1=正常，2=锁定）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "账号状态不能为空")
	@Min(value = 0, message = "账号状态值无效")
	@Max(value = 2, message = "账号状态值无效")
	private Integer status;

	@Schema(title = "性别（0=未知，1=男，2=女）")
	@Min(value = 0, message = "性别值无效")
	@Max(value = 2, message = "性别值无效")
	private Integer gender;

	@Schema(title = "出生日期")
	@PastOrPresent(message = "出生日期不能晚于今天")
	private LocalDate birthday;

	@Schema(title = "个人简介")
	private String introduction;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符")
	private String remark;

}
