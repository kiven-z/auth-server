package com.auth.service.system.schedule.model.vo;

import java.io.Serial;
import java.time.LocalDateTime;

import com.auth.common.core.model.response.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时任务详情
 *
 * @author Bunny
 */
@Schema(name = "SysJobDetailVO", title = "定时任务详情")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysJobDetailVO extends BaseResponse {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "白名单任务类")
	private String jobClass;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "Cron")
	private String cronExpression;

	@Schema(title = "错失策略")
	private Integer misfirePolicy;

	@Schema(title = "是否并发(true=允许并发执行，false=禁止)")
	private Boolean concurrent;

	@Schema(title = "开始时间")
	private LocalDateTime startTime;

	@Schema(title = "结束时间")
	private LocalDateTime endTime;

	@Schema(title = "运行状态(true=正常调度，false=暂停)")
	private Boolean status;

	@Schema(title = "执行参数 JSON")
	private String jobParams;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "上次触发时间")
	private LocalDateTime previousFireTime;

	@Schema(title = "下次触发时间")
	private LocalDateTime nextFireTime;

}
