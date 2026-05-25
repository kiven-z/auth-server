package com.auth.service.system.authorization.outbox.model;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 授权失效 Outbox，对应 {@code sys_authorization_invalidation_outbox}。
 *
 * @author Bunny
 */
@TableName("sys_authorization_invalidation_outbox")
@Getter
@Setter
public class SysAuthorizationInvalidationOutboxEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 业务幂等键，与
	 * {@link com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest#eventId()}
	 * 一致。
	 */
	private String eventId;

	/**
	 * {@link com.auth.module.security.contract.api.authorization.AuthorizationChangeKind}
	 * 枚举名。
	 */
	private String changeKind;

	/**
	 * {@link com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidatePayload}
	 * 的 JSON。
	 */
	private String payload;

	/**
	 * 投递状态：PENDING / PROCESSING / SUCCESS / FAILED / DEAD。
	 */
	private String status;

	private Integer retryCount;

	private Integer maxRetry;

	private LocalDateTime nextRetryAt;

	private String lastError;

	private String lockedBy;

	private LocalDateTime lockedAt;

	private LocalDateTime processedAt;

	/**
	 * 触发模块，如 SYS_ROLE。
	 */
	private String sourceModule;

	/**
	 * 业务主键，如 roleCode。
	 */
	private String sourceBizId;

}
