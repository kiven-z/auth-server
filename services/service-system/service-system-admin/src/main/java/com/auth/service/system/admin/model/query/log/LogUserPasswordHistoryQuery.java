package com.auth.service.system.admin.model.query.log;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 密码历史日志分页查询
 *
 * @author Bunny
 */
@Schema(name = "LogUserPasswordHistoryQuery", title = "密码历史日志查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LogUserPasswordHistoryQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "修改时间(开始)")
	private LocalDateTime changeTimeStart;

	@Schema(title = "修改时间(结束)")
	private LocalDateTime changeTimeEnd;

	@Schema(title = "修改IP地址")
	private String changeIp;

}
