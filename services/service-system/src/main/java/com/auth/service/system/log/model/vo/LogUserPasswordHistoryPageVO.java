package com.auth.service.system.log.model.vo;

import com.auth.common.core.annotation.Desensitized;
import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.desensitize.DesensitizedType;
import com.auth.common.core.model.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 密码历史日志分页行
 *
 * @author Bunny
 */
@Schema(name = "LogUserPasswordHistoryPageVO", title = "密码历史日志分页行")
@Getter
@Setter
@ToString
public class LogUserPasswordHistoryPageVO extends BaseResponse {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "用户名")
	private String username;

	@Desensitized(DesensitizedType.IP_ADDRESS)
	@Schema(title = "修改IP地址")
	private String changeIp;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "修改时间")
	private LocalDateTime changeTime;

}
