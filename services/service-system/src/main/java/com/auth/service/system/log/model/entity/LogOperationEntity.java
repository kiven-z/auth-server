package com.auth.service.system.log.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 操作日志，对应 {@code log_operation}
 *
 * @author Bunny
 */
@TableName("log_operation")
@Getter
@Setter
@Accessors(chain = true)
public class LogOperationEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "操作用户 ID")
	private Long userId;

	@Schema(title = "操作类型")
	private String operationType;

	@Schema(title = "业务模块")
	private String module;

	@Schema(title = "目标类型")
	private String targetType;

	@Schema(title = "目标 ID")
	private String targetId;

	@Schema(title = "HTTP 方法")
	private String requestMethod;

	@Schema(title = "请求 URI")
	private String requestUri;

	@Schema(title = "请求参数摘要")
	private String requestParams;

	@Schema(title = "请求体摘要")
	private String requestBody;

	@Schema(title = "HTTP 状态码")
	private Integer responseStatus;

	@Schema(title = "响应消息摘要")
	private String responseMessage;

	@Schema(title = "执行耗时毫秒")
	private Integer executionTimeMs;

	@Schema(title = "变更前 JSON")
	private String oldValue;

	@Schema(title = "变更后 JSON")
	private String newValue;

	@Schema(title = "客户端 IP")
	private String ipAddress;

	@Schema(title = "User-Agent")
	private String userAgent;

	@Schema(title = "逻辑删除")
	private Boolean isDeleted;

}
