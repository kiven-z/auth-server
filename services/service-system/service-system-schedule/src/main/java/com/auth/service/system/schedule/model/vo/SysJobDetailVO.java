package com.auth.service.system.schedule.model.vo;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;

import com.auth.common.core.model.response.BaseResponse;
import com.auth.service.system.schedule.model.enums.SysJobLastExecutionStatus;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 定时任务详情
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysJobDetailVO extends BaseResponse implements SysJobLastExecutionView {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "任务分组名称")
	private String jobGroupName;

	@Schema(title = "任务类")
	private String jobClass;

	@Schema(title = "任务类型", description = "BEAN_INVOKE/CUSTOM_CLASS/MESSAGE_DISPATCH")
	private String taskType;

	@Schema(title = "执行处理器")
	private String handlerCode;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "Cron 表达式")
	private String cronExpression;

	@Schema(title = "错失策略")
	private Integer misfirePolicy;

	@Schema(title = "是否并发", description = "true=允许并发执行，false=禁止")
	private Boolean concurrent;

	@Schema(title = "开始时间")
	private LocalDateTime startTime;

	@Schema(title = "结束时间")
	private LocalDateTime endTime;

	@Schema(title = "运行状态", description = "true=正常调度，false=暂停")
	private Boolean status;

	@Schema(title = "执行参数 JSON")
	private String jobParams;

	@Schema(title = "扩展载荷 JSON")
	private String payloadJson;

	@Schema(title = "备注")
	private String remark;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "上次触发")
	private Date previousFireTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "下次触发")
	private Date nextFireTime;

	@Schema(title = "运行态", description = "Quartz 实时状态，与 sys_job.status 启停无关")
	private SysJobQuartzRuntimeStatus quartzRuntimeStatus;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "本次开始执行时间", description = "RUNNING 时本次触发的开始时间")
	private Date quartzFireTime;

	@Schema(title = "最近结果", description = "SUCCESS/FAILED/UNKNOWN")
	private SysJobLastExecutionStatus lastExecutionStatus;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "最近执行时间")
	private LocalDateTime lastExecutionTime;

}
