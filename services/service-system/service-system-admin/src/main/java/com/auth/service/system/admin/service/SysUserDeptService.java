package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;

import java.util.List;

/**
 * 用户部门关联管理
 *
 * @author Bunny
 */
public interface SysUserDeptService {

	/**
	 * 分页查询用户部门关联
	 * @param userId 用户 ID
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PageResponse<UserDeptPageVO> getPage(Long userId, UserDeptPageQuery query);

	/**
	 * 新增用户部门关联
	 * @param form 表单
	 */
	void create(UserDeptAssignForm form);

	/**
	 * 更新用户部门关联
	 * @param form 表单
	 */
	void update(UserDeptAssignForm form);

	/**
	 * 设为主部门
	 * @param userId 用户 ID
	 * @param deptId 部门 ID
	 */
	void setPrimary(Long userId, Long deptId);

	/**
	 * 调离（status=0）
	 * @param userId 用户 ID
	 * @param deptId 部门 ID
	 */
	void transfer(Long userId, Long deptId);

	/**
	 * 批量删除用户部门关联
	 * @param userId 用户 ID
	 * @param deptIds 部门 ID 列表
	 */
	void deleteByDeptIds(Long userId, List<Long> deptIds);

}
