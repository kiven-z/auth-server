package com.auth.service.system.log.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 密码历史日志详情
 *
 * @author Bunny
 */
@Schema(name = "LogUserPasswordHistoryDetailVO", title = "密码历史日志详情")
@Data
public class LogUserPasswordHistoryDetailVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "主键")
	private Long id;

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "修改时间")
	private LocalDateTime changeTime;

	@Schema(title = "修改IP地址")
	private String changeIp;

	@Schema(title = "修改时间")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

}
