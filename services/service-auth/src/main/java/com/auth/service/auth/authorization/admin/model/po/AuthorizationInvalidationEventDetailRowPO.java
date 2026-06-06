package com.auth.service.auth.authorization.admin.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权失效幂等事件详情（PO）
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationEventDetailRowPO", title = "授权失效幂等事件详情")
@Getter
@Setter
public class AuthorizationInvalidationEventDetailRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	private String eventId;

	private String changeKind;

	private Integer impactedUserCount;

	private Integer versionBumpedCount;

	private Integer profileRefreshedCount;

	private Integer profileEvictedCount;

	private LocalDateTime processedAt;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private Long createUser;

	private Long updateUser;

	private String remark;

}
