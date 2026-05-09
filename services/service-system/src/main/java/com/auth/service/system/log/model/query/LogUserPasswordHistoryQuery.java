package com.auth.service.system.log.model.query;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.auth.common.core.model.query.PageQueryRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 密码历史日志分页查询
 *
 * @author Bunny
 */
@Schema(name = "LogUserPasswordHistoryQuery", title = "密码历史日志查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class LogUserPasswordHistoryQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID（精确匹配）
	 */
	@Schema(title = "用户ID")
	private Long userId;

	/**
	 * 修改时间-开始
	 */
	@Schema(title = "修改时间(开始)")
	private LocalDateTime changeTimeStart;

	/**
	 * 修改时间-结束
	 */
	@Schema(title = "修改时间(结束)")
	private LocalDateTime changeTimeEnd;

	/**
	 * 修改IP地址（模糊查询）
	 */
	@Schema(title = "修改IP地址")
	private String changeIp;

}
