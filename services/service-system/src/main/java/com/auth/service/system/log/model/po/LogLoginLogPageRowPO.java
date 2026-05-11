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

	/**
	 * 用户ID（未认证成功时为 NULL）
	 */
	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long userId;

	/**
	 * 登录账号（冗余，便于查询）
	 */
	@Schema(title = "登录账号")
	private String username;

	/**
	 * 结果：0=成功，1=密码错误，2=账号锁定，3=验证码错误，4=账号禁用
	 */
	@Schema(title = "登录结果")
	private Integer loginResult;

	/**
	 * 登录尝试时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "登录时间")
	private LocalDateTime loginTime;

	/**
	 * 登录地区（由 IP 解析）
	 */
	@Schema(title = "登录地区")
	private String loginRegion;

	/**
	 * 登录方式
	 */
	@Schema(title = "登录方式")
	private String loginType;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	/**
	 * 创建用户
	 */
	@JsonStringFormat
	@Schema(title = "创建用户")
	private Long createUser;

	/**
	 * 更新用户
	 */
	@JsonStringFormat
	@Schema(title = "更新用户")
	private Long updateUser;

}
