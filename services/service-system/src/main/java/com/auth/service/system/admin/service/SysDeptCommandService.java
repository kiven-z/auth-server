package com.auth.service.system.admin.service;

import com.auth.service.system.admin.model.form.dept.SysDeptCreateForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.form.dept.SysDeptUpdateForm;

/**
 * 部门写操作（增删改、移动）；树结构变更经
 * {@link com.auth.service.system.admin.support.dept.DeptClosureMaintainer}。
 */
public interface SysDeptCommandService {

	/**
	 * 新增部门并写入闭包表。
	 * @param form 新增表单
	 */
	void create(SysDeptCreateForm form);

	/**
	 * 更新部门元数据（名称、编码、状态等），不修改父部门。
	 * @param form 更新表单
	 */
	void updateMeta(SysDeptUpdateForm form);

	/**
	 * 变更父部门并重算闭包。
	 * @param deptId 部门 ID
	 * @param form 移动表单
	 */
	void move(Long deptId, SysDeptMoveForm form);

	/**
	 * 逻辑删除部门（不调用闭包删除存储过程）。
	 * @param deptId 部门 ID
	 */
	void deleteById(Long deptId);

}
