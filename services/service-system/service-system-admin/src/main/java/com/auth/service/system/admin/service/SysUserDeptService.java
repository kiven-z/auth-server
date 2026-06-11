package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.form.userdept.UserDeptAssignForm;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.userdept.UserDeptPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户部门关联管理
 *
 * @author Bunny
 */
public interface SysUserDeptService extends IService<UserDeptEntity> {

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
	 * @param form 关联表单
	 */
	void create(Long userId, UserDeptAssignForm form);

}
