package com.auth.service.system.admin.model.vo.post;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位详情-所属部门项
 *
 * @author Bunny
 */
@Schema(name = "SysPostBoundDeptItemVO", title = "岗位所属部门项")
@Getter
@Setter
@ToString
public class SysPostBoundDeptItemVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "部门ID")
	private Long deptId;

	@Schema(title = "部门编码")
	private String deptCode;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "启用状态")
	private Boolean status;

	@JsonStringFormat
	@Schema(title = "父部门ID，0 表示顶级")
	private Long parentId;

}
