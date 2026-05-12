package com.auth.service.system.log.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志分页查询
 *
 * @author Bunny
 */
@Schema(name = "LogLoginLogQuery", title = "登录日志查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class LogLoginLogQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户ID")
	private Long userId;

	@Schema(title = "登录结果")
	private Integer loginResult;

	@Schema(title = "登录时间(开始)")
	private LocalDateTime loginTimeStart;

	@Schema(title = "登录时间(结束)")
	private LocalDateTime loginTimeEnd;

	@Schema(title = "登录方式")
	private String loginType;

}
