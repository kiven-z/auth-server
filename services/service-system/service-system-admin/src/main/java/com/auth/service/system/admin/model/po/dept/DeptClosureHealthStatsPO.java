package com.auth.service.system.admin.model.po.dept;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 闭包健康度统计
 *
 * @author bunny
 */
@Getter
@Setter
@ToString
public class DeptClosureHealthStatsPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long onlySelf;

	private Long hasAncestors;

	private Long zeroClosure;

	private Long total;

}
