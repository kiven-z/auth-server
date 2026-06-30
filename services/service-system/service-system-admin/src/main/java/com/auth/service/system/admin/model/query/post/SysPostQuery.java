package com.auth.service.system.admin.model.query.post;

import java.io.Serial;
import java.io.Serializable;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 岗位分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "SysPostQuery", title = "岗位分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysPostQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "岗位编码（模糊）")
	private String postCode;

	@Schema(title = "岗位名称（模糊）")
	private String postName;

	@Schema(title = "部门名称（模糊）")
	private String deptName;

	@Schema(title = "启用状态(true=启用，false=停用)")
	private Boolean status;

}
