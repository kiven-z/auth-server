package com.auth.service.system.log.model.po;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.common.core.annotation.Desensitized;
import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.desensitize.DesensitizedType;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 密码历史日志分页行（持久层查询投影，非 API 出参）
 *
 * @author Bunny
 */
@Schema(name = "LogUserPasswordHistoryPageRowPO", title = "密码历史日志分页行（PO）")
@Getter
@Setter
@ToString
public class LogUserPasswordHistoryPageRowPO implements Serializable {

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

	@Desensitized(DesensitizedType.IP_ADDRESS)
	@Schema(title = "修改IP地址")
	private String changeIp;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "修改时间")
	private LocalDateTime changeTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@JsonStringFormat
	@Schema(title = "创建用户")
	private Long createUser;

	@JsonStringFormat
	@Schema(title = "更新用户")
	private Long updateUser;

}
