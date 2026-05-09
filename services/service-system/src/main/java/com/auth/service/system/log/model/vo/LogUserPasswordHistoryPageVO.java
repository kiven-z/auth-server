package com.auth.service.system.log.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 密码历史日志分页行
 *
 * @author Bunny
 */
@Schema(name = "LogUserPasswordHistoryPageVO", title = "密码历史日志分页行")
@Data
@EqualsAndHashCode(callSuper = true)
public class LogUserPasswordHistoryPageVO extends BaseResponse {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "修改时间")
	private LocalDateTime changeTime;

	@Schema(title = "修改IP地址")
	private String changeIp;

}
