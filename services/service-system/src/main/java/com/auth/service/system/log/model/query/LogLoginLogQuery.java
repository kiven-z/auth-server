package com.auth.service.system.log.model.query;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.common.core.model.query.PageQueryRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

	/**
	 * 用户 ID（远程搜索选中）
	 */
	@Schema(title = "用户ID")
	private Long userId;

	/**
	 * 登录结果：0=成功，1=密码错误，2=账号锁定，3=验证码错误，4=账号禁用
	 */
	@Schema(title = "登录结果")
	private Integer loginResult;

	/**
	 * 登录时间(开始)
	 */
	@Schema(title = "登录时间(开始)")
	private LocalDateTime loginTimeStart;

	/**
	 * 登录时间(结束)
	 */
	@Schema(title = "登录时间(结束)")
	private LocalDateTime loginTimeEnd;

	/**
	 * 登录方式：PASSWORD/EMAIL
	 */
	@Schema(title = "登录方式")
	private String loginType;

}
