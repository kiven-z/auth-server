package com.auth.service.system.admin.model.po.dept;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 闭包父子直连异常行（Mapper 结果）
 *
 * @author bunny
 */
@Getter
@Setter
@ToString
public class DeptClosureParentLinkRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	private String deptName;

	private Long parentId;

}
