package com.auth.service.system.schedule.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 定时任务分组（对应 Quartz {@code JOB_GROUP} 展示与字典）
 *
 * @author Bunny
 */
@TableName("sys_job_group")
@Schema(name = "SysJobGroupEntity", title = "定时任务分组")
@Getter
@Setter
@Accessors(chain = true)
public class SysJobGroupEntity extends BaseEntity {

	@Schema(title = "分组编码(大写)")
	private String groupCode;

	@Schema(title = "显示名称")
	private String groupName;

	@Schema(title = "描述")
	private String description;

	@Schema(title = "状态(1启用 0禁用)")
	private Integer status;

	@Schema(title = "是否系统内置(1是)")
	private Integer isSystem;

	@Schema(title = "排序号")
	private Integer orderNum;

}
