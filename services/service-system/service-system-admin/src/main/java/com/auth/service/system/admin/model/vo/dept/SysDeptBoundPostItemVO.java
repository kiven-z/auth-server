package com.auth.service.system.admin.model.vo.dept;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门详情-下属岗位项
 */
@Schema(name = "SysDeptBoundPostItemVO", title = "部门下属岗位项")
@Getter
@Setter
public class SysDeptBoundPostItemVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "岗位ID")
	@JsonStringFormat
	private Long id;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态(true=启用，false=停用)")
	private Boolean status;

}
