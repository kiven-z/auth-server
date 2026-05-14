package com.auth.service.system.log.model.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志分页行（持久层投影）。
 *
 * @author Bunny
 */
@Schema(name = "LogOperationPageRowPO", title = "操作日志分页行（PO）")
@Getter
@Setter
@ToString
public class LogOperationPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@Schema(title = "操作用户 ID")
	private Long userId;

	@Schema(title = "操作模块")
	private String module;

	@Schema(title = "操作类型")
	private String operationType;

	@Schema(title = "目标类型")
	private String targetType;

	@Schema(title = "目标主键 ID")
	private Long targetId;

	@Schema(title = "HTTP 方法")
	private String requestMethod;

	@Schema(title = "HTTP 状态码")
	private Integer responseStatus;

	@Schema(title = "执行耗时毫秒")
	private Integer executionTimeMs;

	@Schema(title = "客户端 IP（原始）")
	private String ipAddress;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@Schema(title = "创建人")
	private Long createUser;

	@Schema(title = "更新人")
	private Long updateUser;

}
