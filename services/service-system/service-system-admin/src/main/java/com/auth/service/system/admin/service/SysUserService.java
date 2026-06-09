package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.form.user.SysUserBatchStatusForm;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.model.form.user.SysUserUpdateForm;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统用户服务
 *
 * @author Bunny
 */
public interface SysUserService extends IService<UserEntity> {

	/**
	 * 用户分页查询
	 * @param pageParams 分页参数
	 * @param query 筛选条件
	 * @return 分页结果
	 */
	PageResponse<SysUserPageVO> getPage(Page<UserEntity> pageParams, SysUserPageQuery query);

	/**
	 * 新增用户
	 * @param form 用户资料
	 */
	void create(SysUserForm form);

	/**
	 * 更新用户基础资料（不含用户名与账号状态）
	 * @param id 用户主键
	 * @param form 更新表单
	 */
	void update(Long id, SysUserUpdateForm form);

	/**
	 * 批量逻辑删除用户
	 * @param ids 用户主键列表
	 */
	void deleteByIds(List<Long> ids);

	/**
	 * 批量更新用户账号状态
	 * @param form 目标状态与用户主键列表
	 */
	void batchUpdateStatus(SysUserBatchStatusForm form);

}
