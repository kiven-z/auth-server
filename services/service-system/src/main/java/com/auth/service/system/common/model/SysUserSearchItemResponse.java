package com.auth.service.system.common.model;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户关键词搜索单行结果（不含敏感字段）
 *
 * @author Bunny
 */
@Schema(name = "SysUserSearchItemResponse", title = "用户搜索项")
@Getter
@Setter
public class SysUserSearchItemResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long id;

	@Schema(title = "登录账号")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

}
