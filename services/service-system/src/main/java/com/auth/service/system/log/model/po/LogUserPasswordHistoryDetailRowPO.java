package com.auth.service.system.log.model.po;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.common.core.annotation.Desensitized;
import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.desensitize.DesensitizedType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 密码历史日志详情（持久层查询投影，非 API 出参）
 *
 * @author Bunny
 */
@Schema(name = "LogUserPasswordHistoryDetailRowPO", title = "密码历史日志详情（PO）")
@Getter
@Setter
@ToString
public class LogUserPasswordHistoryDetailRowPO implements Serializable {

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

	@Desensitized(DesensitizedType.IP_ADDRESS)
	@Schema(title = "修改IP地址")
	private String changeIp;

	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@JsonStringFormat
	@Schema(title = "创建用户")
	private Long createUser;

	@JsonStringFormat
	@Schema(title = "更新用户")
	private Long updateUser;

}
