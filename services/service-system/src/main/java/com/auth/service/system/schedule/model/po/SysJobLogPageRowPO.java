package com.auth.service.system.schedule.model.po;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 任务日志分页行（持久层查询投影，非 API 出参）
 *
 * @author Bunny
 */
@Schema(name = "SysJobLogPageRowPO", title = "任务日志分页行（PO）")
@Getter
@Setter
@ToString
public class SysJobLogPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

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

	@Schema(title = "耗时毫秒")
	private Long elapsedTime;

	@Schema(title = "记录时间")
	private LocalDateTime createTime;

}
