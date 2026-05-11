package com.auth.service.system.admin.model.po.jobgroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务分组分页行（持久层查询投影，非 API 出参）
 *
 * @author Bunny
 */
@Schema(name = "SysJobGroupPageRowPO", title = "任务分组分页行（PO）")
@Getter
@Setter
@ToString
public class SysJobGroupPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

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

	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@Schema(title = "创建用户")
	private Long createUser;

	@Schema(title = "更新用户")
	private Long updateUser;

}
