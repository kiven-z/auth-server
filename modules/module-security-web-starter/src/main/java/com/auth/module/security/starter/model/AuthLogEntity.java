package com.auth.module.security.starter.model;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 权限决策审计日志
 *
 * @author Bunny
 */
@TableName("log_authorization_audit")
@Getter
@Setter
@Accessors(chain = true)
public class AuthLogEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 事件类型：GRANTED / DENIED
	 */
	private String eventType;

	/**
	 * 用户ID（未认证时为 NULL）
	 */
	private Long userId;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 会话ID（关联 user_session）
	 */
	private String sessionId;

	/**
	 * 所需权限码（如 sys:user:add）
	 */
	private String requiredPermission;

	/**
	 * HTTP 请求方法
	 */
	private String requestMethod;

	/**
	 * 请求 URI
	 */
	private String requestUri;

	/**
	 * 请求 IP
	 */
	private String requestIp;

	/**
	 * 决策原因（枚举值）
	 */
	private String decisionReason;

	/**
	 * 决策详情
	 */
	private String decisionDetail;

	/**
	 * 用户权限摘要
	 */
	private String userPermissionsSummary;

	/**
	 * 用户角色列表（逗号分隔）
	 */
	private String userRoles;

	/**
	 * 命中豁免配置：USER_ID/ROLE_CODE/PATH
	 */
	private String exemptConfigHit;

	/**
	 * 策略编码（ARBAC 场景）
	 */
	private String policyCode;

	/**
	 * 策略决策结果（ARBAC 场景）
	 */
	private Boolean policyDecision;

	/**
	 * Controller 类名
	 */
	private String className;

	/**
	 * 方法名
	 */
	private String methodName;

	/**
	 * 方法参数摘要（JSON）
	 */
	private String methodParams;

	/**
	 * 异常消息
	 */
	private String exceptionMessage;

	/**
	 * 逻辑删除标记
	 */
	@TableLogic(value = "0", delval = "1")
	private Boolean isDeleted;

}
