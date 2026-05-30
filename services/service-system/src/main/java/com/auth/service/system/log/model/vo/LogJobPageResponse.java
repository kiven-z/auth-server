package com.auth.service.system.log.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务日志分页行
 *
 * @author Bunny
 */
@Schema(name = "LogJobPageResponse", title = "任务日志分页行")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LogJobPageResponse extends BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务ID")
	private Long jobId;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "触发类型(SCHEDULE/MANUAL)")
	private String triggerType;

	@Schema(title = "执行报告")
	private String jobMessage;

	@Schema(title = "执行是否成功(true=成功，false=失败)")
	private Boolean status;

	@Schema(title = "耗时毫秒")
	private Long elapsedTime;

}
