package com.auth.service.system.schedule.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import com.auth.service.system.schedule.model.enums.SysJobLastExecutionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务分页行（含最近一次执行结果补充字段）
 *
 * @author Bunny
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysJobPageResponse extends BaseResponse implements Serializable, SysJobLastExecutionView {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "任务类")
	private String jobClass;

	@Schema(title = "任务类型", description = "BEAN_INVOKE/CUSTOM_CLASS")
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

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "开始时间")
	private LocalDateTime startTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "结束时间")
	private LocalDateTime endTime;

	@Schema(title = "运行状态", description = "true=正常调度，false=暂停")
	private Boolean status;

	@Schema(title = "执行参数 JSON", description = "写入 JobDataMap")
	private String jobParams;

	@Schema(title = "扩展载荷 JSON")
	private String payloadJson;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "最近结果", description = "SUCCESS/FAILED/UNKNOWN")
	private SysJobLastExecutionStatus lastExecutionStatus;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "最近执行时间")
	private LocalDateTime lastExecutionTime;

}
