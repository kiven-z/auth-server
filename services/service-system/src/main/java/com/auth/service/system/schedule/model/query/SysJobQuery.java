package com.auth.service.system.schedule.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 定时任务分页查询
 *
 * @author Bunny
 */
@Schema(name = "SysJobQuery", title = "定时任务查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysJobQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "任务名称")
	private String jobName;

	@Schema(title = "任务分组")
	private String jobGroup;

	@Schema(title = "运行状态(true=正常调度，false=暂停)")
	private Boolean status;

}
