package com.auth.service.system.common.feign.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 授权失效幂等事件分页行（Feign 传输）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class AuthorizationInvalidationEventPageInnerDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "业务事件 ID（幂等键）")
	private String eventId;

	@Schema(title = "变更维度")
	private String changeKind;

	@Schema(title = "是否处理中占位（impacted_user_count = -1）")
	private Boolean processing;

	@Schema(title = "影响面用户数")
	private Integer impactedUserCount;

	@Schema(title = "递增版本用户数")
	private Integer versionBumpedCount;

	@Schema(title = "刷新画像用户数")
	private Integer profileRefreshedCount;

	@Schema(title = "驱逐画像用户数")
	private Integer profileEvictedCount;

	@Schema(title = "处理完成时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime processedAt;

	@Schema(title = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateTime;

	@Schema(title = "创建用户")
	private Long createUser;

	@Schema(title = "更新用户")
	private Long updateUser;

}
