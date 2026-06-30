package com.auth.service.system.admin.model.form.user;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 当前用户修改自己的密码表单
 *
 * @author Bunny
 */
@Schema(name = "SysUserChangePasswordForm", title = "用户修改密码")
@Getter
@Setter
public class SysUserChangePasswordForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "旧密码")
	@NotBlank(message = "旧密码不能为空")
	private String oldPassword;

	@Schema(title = "新密码")
	@Size(min = 8, max = 18, message = "新密码长度应为8-18位")
	@NotBlank(message = "新密码不能为空")
	private String newPassword;

	@Schema(title = "确认密码")
	@NotBlank(message = "确认密码不能为空")
	private String confirmPassword;

}
