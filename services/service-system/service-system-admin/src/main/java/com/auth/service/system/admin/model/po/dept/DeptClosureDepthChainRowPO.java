package com.auth.service.system.admin.model.po.dept;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 闭包深度链条异常行
 *
 * @author bunny
 */
@Getter
@Setter
@ToString
public class DeptClosureDepthChainRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	private String deptName;

	private Long parentId;

	private Long childClosureCnt;

	private Long parentClosureCnt;

	private Long expectedChildCnt;

}
