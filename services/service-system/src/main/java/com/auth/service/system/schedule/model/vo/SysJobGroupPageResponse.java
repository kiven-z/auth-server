package com.auth.service.system.schedule.model.vo;

import java.io.Serial;
import java.io.Serializable;

import com.auth.common.core.model.response.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务分组分页行
 *
 * @author Bunny
 */
@Schema(name = "SysJobGroupPageResponse", title = "任务分组分页行")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysJobGroupPageResponse extends BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "分组编码")
	private String groupCode;

	@Schema(title = "显示名称")
	private String groupName;

	@Schema(title = "描述")
	private String description;

	@Schema(title = "启用状态(true=启用，false=停用)")
	private Boolean status;

	@Schema(title = "是否系统内置")
	private Boolean isSystem;

	@Schema(title = "排序号")
	private Integer orderNum;

}
