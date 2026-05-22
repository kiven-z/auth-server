package com.auth.service.system.log.model.po;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

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

	@Schema(title = "任务ID")
	private Long jobId;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "调用目标")
	private String invokeTarget;

	@Schema(title = "触发类型(SCHEDULE/MANUAL)")
	private String triggerType;

	@Schema(title = "执行报告")
	private String jobMessage;

	@Schema(title = "执行是否成功(1=成功，0=失败)")
	private Boolean status;

	@Schema(title = "耗时毫秒")
	private Long elapsedTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@JsonStringFormat
	@Schema(title = "创建用户")
	private Long createUser;

	@JsonStringFormat
	@Schema(title = "更新用户")
	private Long updateUser;

}
