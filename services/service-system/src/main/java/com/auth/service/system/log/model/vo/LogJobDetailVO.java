package com.auth.service.system.log.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 任务日志详情（含异常堆栈）
 *
 * @author Bunny
 */
@Schema(name = "LogJobDetailVO", title = "任务日志详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogJobDetailVO extends BaseResponse {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "执行报告")
	private String jobMessage;

	@Schema(title = "状态(0成功 1失败)")
	private Integer status;

	@Schema(title = "异常信息")
	private String exceptionInfo;

	@Schema(title = "耗时毫秒")
	private Long elapsedTime;

}
