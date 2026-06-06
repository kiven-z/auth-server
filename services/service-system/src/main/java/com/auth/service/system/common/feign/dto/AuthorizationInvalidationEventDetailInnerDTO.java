package com.auth.service.system.common.feign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 授权失效幂等事件详情（Feign 传输）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class AuthorizationInvalidationEventDetailInnerDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	private String eventId;

	private String changeKind;

	private Boolean processing;

	private Integer impactedUserCount;

	private Integer versionBumpedCount;

	private Integer profileRefreshedCount;

	private Integer profileEvictedCount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime processedAt;

	private String remark;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateTime;

	private Long createUser;

	private Long updateUser;

}
