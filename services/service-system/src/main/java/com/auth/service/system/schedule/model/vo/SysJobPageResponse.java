package com.auth.service.system.schedule.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务分页行（含 Quartz 运行态补充字段）
 *
 * @author Bunny
 */
@Schema(name = "SysJobPageResponse", title = "定时任务分页行")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysJobPageResponse extends BaseResponse implements Serializable {

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

	@Schema(title = "是否并发")
	private Integer concurrent;

	@Schema(title = "开始时间")
	private LocalDateTime startTime;

	@Schema(title = "结束时间")
	private LocalDateTime endTime;

	@Schema(title = "状态(0暂停 1正常)")
	private Integer status;

	@Schema(title = "执行参数 JSON，写入 JobDataMap")
	private String jobParams;

	@Schema(title = "备注")
	private String remark;

	@Schema(title = "上次触发时间")
	private LocalDateTime previousFireTime;

	@Schema(title = "下次触发时间")
	private LocalDateTime nextFireTime;

}
