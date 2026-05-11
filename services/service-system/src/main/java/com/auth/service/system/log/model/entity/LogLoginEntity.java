package com.auth.service.system.log.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 登录日志，对应 {@code log_login}。
 * <p>
 * 记录用户每次登录尝试的结果，包含成功和失败信息。该表由 auth-service 写入， system-service 仅提供查询与删除。
 * </p>
 *
 * @author Bunny
 */
@TableName("log_login")
@Schema(name = "LogLoginLogEntity", title = "登录日志")
@Getter
@Setter
@Accessors(chain = true)
public class LogLoginEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID（未认证成功时为 NULL）
	 */
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
	 * 失败原因详情（如"密码错误第3次"）
	 */
	@Schema(title = "失败原因详情")
	private String failureReason;

	/**
	 * 登录尝试时间
	 */
	@Schema(title = "登录时间")
	private LocalDateTime loginTime;

	/**
	 * 登录 IP（脱敏展示）
	 */
	@Schema(title = "登录IP")
	private String loginIp;

	/**
	 * 登录地区（由 IP 解析）
	 */
	@Schema(title = "登录地区")
	private String loginRegion;

	/**
	 * 浏览器/设备 UA
	 */
	@Schema(title = "用户代理")
	private String userAgent;

	/**
	 * 设备类型：PC/MOBILE/TABLET
	 */
	@Schema(title = "设备类型")
	private String deviceType;

	/**
	 * 操作系统
	 */
	@Schema(title = "操作系统")
	private String osType;

	/**
	 * 浏览器类型
	 */
	@Schema(title = "浏览器类型")
	private String browserType;

	/**
	 * 事件类型：LOGIN_EMAIL / LOGIN_PASSWORD / REFRESH_TOKEN / LOGOUT 等
	 */
	@Schema(title = "登录方式")
	private String loginType;

	/**
	 * 登录成功后的会话 ID
	 */
	@Schema(title = "会话ID")
	private String sessionId;

}
