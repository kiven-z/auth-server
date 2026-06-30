package com.auth.service.system.admin.model.query.log;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录日志分页查询
 *
 * @author Bunny
 */
@Schema(name = "LogLoginLogQuery", title = "登录日志查询")
@Getter
@Setter
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
