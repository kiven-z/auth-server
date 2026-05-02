package com.auth.service.system.schedule.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务分组分页查询
 *
 * @author Bunny
 */
@Schema(name = "SysJobGroupQuery", title = "任务分组查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysJobGroupQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "分组编码")
	private String groupCode;

	@Schema(title = "显示名称")
	private String groupName;

	@Schema(title = "状态(1启用 0禁用)")
	private Integer status;

}
