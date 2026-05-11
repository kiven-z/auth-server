package com.auth.service.system.log.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.common.core.annotation.Desensitized;
import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.desensitize.DesensitizedType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录日志详情
 *
 * @author Bunny
 */
@Schema(name = "LogLoginLogDetailVO", title = "登录日志详情")
@Data
public class LogLoginLogDetailVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
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
	 * 登录 IP
	 */
	@Desensitized(DesensitizedType.IP_ADDRESS)
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
	 * 登录方式：PASSWORD/EMAIL
	 */
	@Schema(title = "登录方式")
	private String loginType;

	/**
	 * 登录成功后的会话 ID
	 */
	@Schema(title = "会话ID")
	private String sessionId;

	/**
	 * 创建时间
	 */
	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

}
