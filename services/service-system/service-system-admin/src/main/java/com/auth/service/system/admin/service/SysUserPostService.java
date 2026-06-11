package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.UserPostEntity;
import com.auth.service.system.admin.model.query.user.UserPostPageQuery;
import com.auth.service.system.admin.model.vo.userpost.UserPostPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户岗位关联管理
 *
 * @author Bunny
 */
public interface SysUserPostService extends IService<UserPostEntity> {

	/**
	 * 分页查询用户岗位关联
	 * @param userId 用户 ID
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PageResponse<UserPostPageVO> getPage(Long userId, UserPostPageQuery query);

}
