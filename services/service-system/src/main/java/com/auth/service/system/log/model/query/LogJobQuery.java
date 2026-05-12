package com.auth.service.system.log.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务日志分页查询
 *
 * @author Bunny
 */
@Schema(name = "LogJobQuery", title = "任务日志查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LogJobQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "耗时(ms)")
	private Long elapsedTime;

	@Schema(title = "状态(0成功 1失败)")
	private Integer status;

}
