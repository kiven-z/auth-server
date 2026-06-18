package com.auth.service.system.admin.model.vo.role;

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
 * 角色详情-已授权岗位项
 *
 * @author Bunny
 */
@Schema(name = "SysRoleBoundPostItemVO", title = "角色已授权岗位项")
@Getter
@Setter
@ToString
public class SysRoleBoundPostItemVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "岗位ID")
	private Long postId;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态(true=启用，false=停用)")
	private Boolean status;

	@JsonStringFormat
	@Schema(title = "所属部门ID")
	private Long deptId;

	@Schema(title = "所属部门名称")
	private String deptName;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

}
