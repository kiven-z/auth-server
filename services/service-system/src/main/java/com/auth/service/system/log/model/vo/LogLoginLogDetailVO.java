package com.auth.service.system.log.model.vo;

import com.auth.common.core.annotation.Desensitized;
import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.desensitize.DesensitizedType;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 登录日志详情
 *
 * @author Bunny
 */
@Schema(name = "LogLoginLogDetailVO", title = "登录日志详情")
@Data
@EqualsAndHashCode(callSuper = true)
public class LogLoginLogDetailVO extends BaseResponse {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "登录账号")
	private String username;

	@Schema(title = "登录结果")
	private Integer loginResult;

	@Schema(title = "失败原因详情")
	private String failureReason;

	@Schema(title = "登录时间")
	private LocalDateTime loginTime;

	@Desensitized(DesensitizedType.IP_ADDRESS)
	@Schema(title = "登录IP")
	private String loginIp;

	@Schema(title = "登录地区")
	private String loginRegion;

	@Schema(title = "用户代理")
	private String userAgent;

	@Schema(title = "设备类型")
	private String deviceType;

	@Schema(title = "操作系统")
	private String osType;

	@Schema(title = "浏览器类型")
	private String browserType;

	@Schema(title = "登录方式")
	private String loginType;

	@Schema(title = "会话ID")
	private String sessionId;

}
