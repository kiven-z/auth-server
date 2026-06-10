package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.form.userdept.UserDeptUpdateForm;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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
	 * @param userId 用户 ID
	 * @param form 表单
	 */
	void create(Long userId, UserDeptAssignForm form);

	/**
	 * 更新用户部门关联
	 * @param userId 用户 ID
	 * @param relationId 关联主键
	 * @param form 表单
	 */
	void update(Long userId, Long relationId, UserDeptUpdateForm form);

	/**
	 * 设为主部门
	 * @param userId 用户 ID
	 * @param relationId 关联主键
	 */
	void setPrimary(Long userId, Long relationId);

	/**
	 * 调离（status=0）
	 * @param userId 用户 ID
	 * @param relationId 关联主键
	 */
	void transfer(Long userId, Long relationId);

	/**
	 * 批量删除用户部门关联
	 * @param userId 用户 ID
	 * @param relationIds 关联主键列表
	 */
	void deleteByIds(Long userId, List<Long> relationIds);

}
