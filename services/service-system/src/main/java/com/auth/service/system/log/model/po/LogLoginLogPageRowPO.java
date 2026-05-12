package com.auth.service.system.log.model.po;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 登录日志分页行（持久层查询投影，非 API 出参）
 *
 * @author Bunny
 */
@Schema(name = "LogLoginLogPageRowPO", title = "登录日志分页行（PO）")
@Getter
@Setter
@ToString
public class LogLoginLogPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "主键")
	private Long id;

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "登录账号")
	private String username;

	@Schema(title = "登录结果")
	private Integer loginResult;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "登录时间")
	private LocalDateTime loginTime;

	@Schema(title = "登录地区")
	private String loginRegion;

	@Schema(title = "登录方式")
	private String loginType;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@JsonStringFormat
	@Schema(title = "创建用户")
	private Long createUser;

	@JsonStringFormat
	@Schema(title = "更新用户")
	private Long updateUser;

}
