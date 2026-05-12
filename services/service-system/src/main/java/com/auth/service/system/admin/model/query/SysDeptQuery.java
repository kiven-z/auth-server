package com.auth.service.system.admin.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门扁平列表 / 分页查询条件（与文档「部门管理」查询条件对齐）。
 *
 * @author Bunny
 */
@Schema(name = "SysDeptQuery", title = "部门列表查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDeptQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "部门名称（模糊）")
	private String deptName;

	@Schema(title = "部门编码（模糊）")
	private String deptCode;

	@Schema(title = "状态(true=正常，false=禁用)")
	private Boolean status;

}
