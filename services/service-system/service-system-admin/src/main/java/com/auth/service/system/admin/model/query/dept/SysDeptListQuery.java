package com.auth.service.system.admin.model.query.dept;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门扁平列表查询（无分页，供前端建树）
 *
 * @author Bunny
 */
@Schema(name = "SysDeptListQuery", title = "部门扁平列表查询")
@Getter
@Setter
public class SysDeptListQuery implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "关键词（部门名称或编码，模糊；含命中节点的祖先）")
	private String keyword;

	@Schema(title = "状态(true=正常，false=禁用)")
	private Boolean status;

}
