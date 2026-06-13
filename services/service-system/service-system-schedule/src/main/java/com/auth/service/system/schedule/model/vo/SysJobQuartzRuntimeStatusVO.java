package com.auth.service.system.schedule.model.vo;

import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Quartz 任务运行时状态
 *
 * @author Bunny
 */
@Schema(name = "SysJobQuartzRuntimeStatusVO", title = "Quartz 任务运行时状态")
@Getter
@Setter
@ToString
public class SysJobQuartzRuntimeStatusVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "Quartz 运行时状态")
	private SysJobQuartzRuntimeStatus status;

	@Schema(title = "本次开始执行时间（RUNNING 时有值）")
	private LocalDateTime fireTime;

	@Schema(title = "最近一次失败时间（FAILED 时有值）")
	private LocalDateTime lastFailedTime;

}
