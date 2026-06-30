package com.auth.service.system.admin.model.form.me;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 当前用户更新个人资料表单
 *
 * @author Bunny
 */
@Schema(name = "MeProfileUpdateForm", title = "当前用户资料更新")
@Getter
@Setter
public class MeProfileUpdateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "昵称", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 64, message = "昵称长度不能超过64个字符")
	@NotBlank(message = "昵称不能为空")
	private String nickname;

	@Schema(title = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 128, message = "邮箱长度不能超过128个字符")
	@Email(message = "邮箱格式不正确")
	@NotBlank(message = "邮箱不能为空")
	private String email;

	@Schema(title = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 32, message = "手机号长度不能超过32个字符")
	@NotBlank(message = "手机号不能为空")
	private String phone;

	@Schema(title = "性别（0=未知，1=男，2=女）")
	@Max(value = 2, message = "性别值无效")
	@Min(value = 0, message = "性别值无效")
	private Integer gender;

	@Schema(title = "出生日期")
	@PastOrPresent(message = "出生日期不能晚于今天")
	private LocalDate birthday;

	@Schema(title = "个人简介")
	private String introduction;

	@Schema(title = "头像 URL")
	@Size(max = 512, message = "头像地址长度不能超过512个字符")
	private String avatar;

}
