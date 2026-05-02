package com.auth.service.system.schedule.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 定时任务业务主表（与 QRTZ_* 中 JobKey 一致）
 *
 * @author Bunny
 */
@TableName("sys_job")
@Schema(name = "SysJobEntity", title = "定时任务")
@Getter
@Setter
public class SysJobEntity extends BaseEntity {

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "白名单任务类全限定名")
	private String jobClass;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "Cron 表达式")
	private String cronExpression;

	@Schema(title = "错失策略(1/2/3)")
	private Integer misfirePolicy;

	@Schema(title = "是否并发(0否 1是)")
	private Boolean concurrent;

	@Schema(title = "开始时间")
	private LocalDateTime startTime;

	@Schema(title = "结束时间")
	private LocalDateTime endTime;

	@Schema(title = "状态(0暂停 1正常)")
	private Boolean status;

	@Schema(title = "执行参数 JSON")
	private String jobParams;

	@Schema(title = "备注")
	private String remark;

}
