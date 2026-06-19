package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 部门写服务。
 *
 * @author Bunny
 */
public interface SysDeptService extends IService<SysDeptEntity> {

	/**
	 * 新增部门并写入闭包表
	 * @param form 新增表单
	 */
	void create(SysDeptForm form);

	/**
	 * 更新部门元数据
	 * @param form 保存表单
	 */
	void updateMeta(SysDeptForm form);

	/**
	 * 变更父部门并重算闭包
	 * @param deptId 部门 ID
	 * @param form 移动表单
	 */
	void move(Long deptId, SysDeptMoveForm form);

	/**
	 * 逻辑删除部门
	 * @param deptId 部门 ID
	 */
	void deleteById(Long deptId);

}
