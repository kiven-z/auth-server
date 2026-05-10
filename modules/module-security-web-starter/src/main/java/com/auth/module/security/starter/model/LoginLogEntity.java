package com.auth.module.security.starter.model;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 登录日志（log_login）
 *
 * <p>
 * 记录用户每次登录尝试的结果，包括成功和失败。失败时会记录具体原因和剩余次数。
 * </p>
 *
 * @author Bunny
 */
@TableName("log_login")
@Getter
@Setter
@Accessors(chain = true)
public class LoginLogEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID（未认证成功时为 NULL）
	 */
	private Long userId;

	/**
	 * 登录账号（冗余，便于查询）
	 */
	private String username;

	/**
	 * 结果：0=成功，1=密码错误，2=账号锁定，3=验证码错误，4=账号禁用
	 */
	private Integer loginResult;

	/**
	 * 失败原因详情（如"密码错误第3次"）
	 */
	private String failureReason;

	/**
	 * 登录尝试时间
	 */
	private LocalDateTime loginTime;

	/**
	 * 登录 IP
	 */
	private String loginIp;

	/**
	 * 登录地区（由 IP 解析）
	 */
	private String loginRegion;

	/**
	 * 浏览器/设备 UA
	 */
	private String userAgent;

	/**
	 * 设备类型：PC/MOBILE/TABLET
	 */
	private String deviceType;

	/**
	 * 操作系统
	 */
	private String osType;

	/**
	 * 浏览器类型
	 */
	private String browserType;

	/**
	 * 事件类型：LOGIN_EMAIL / LOGIN_PASSWORD / REFRESH_TOKEN / LOGOUT 等
	 */
	private String loginType;

	/**
	 * 登录成功后的会话ID
	 */
	private String sessionId;

}
