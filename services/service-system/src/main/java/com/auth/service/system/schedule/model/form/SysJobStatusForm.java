package com.auth.service.system.schedule.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量更新任务状态并同步 Quartz pause/resume
 *
 * @author Bunny
 */
@Schema(name = "SysJobStatusForm", title = "任务状态批量变更")
@Data
public class SysJobStatusForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务主键列表")
	@NotEmpty
	private List<Long> ids;

	@Schema(title = "状态(0暂停 1正常)")
	@NotNull
	private Boolean status;

}
