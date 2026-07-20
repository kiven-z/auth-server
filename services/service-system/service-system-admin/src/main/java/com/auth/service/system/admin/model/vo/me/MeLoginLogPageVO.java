package com.auth.service.system.admin.model.vo.me;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 个人中心登录日志分页行（精简字段）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class MeLoginLogPageVO {

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "登录时间")
	private LocalDateTime loginTime;

	@Schema(title = "登录地区")
	private String loginRegion;

	@Schema(title = "登录结果")
	private Integer loginResult;

	@Schema(title = "登录方式")
	private String loginType;

}
