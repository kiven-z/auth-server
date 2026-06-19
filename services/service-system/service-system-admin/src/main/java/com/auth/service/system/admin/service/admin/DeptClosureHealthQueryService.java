package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthVO;

/**
 * 部门闭包表健康检查只读查询。
 *
 * @author bunny
 */
public interface DeptClosureHealthQueryService {

	/**
	 * 执行闭包表一致性诊断
	 * @return 健康检查结果
	 */
	DeptClosureHealthVO inspect();

}
