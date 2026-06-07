package com.auth.service.system.admin.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门分页 / 导出查询
 *
 * @author Bunny
 */
@Schema(name = "SysDeptPageQuery", title = "部门分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysDeptPageQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "部门名称（模糊）")
	private String deptName;

	@Schema(title = "部门编码（模糊）")
	private String deptCode;

	@Schema(title = "状态(true=正常，false=禁用)")
	private Boolean status;

}
