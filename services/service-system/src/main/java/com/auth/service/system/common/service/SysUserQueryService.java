package com.auth.service.system.common.service;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.common.model.SysUserSearchItemResponse;
import com.auth.service.system.common.model.vo.SysUserProfileResponse;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统用户只读查询（档案聚合、关键词搜索等）。
 *
 * @author Bunny
 */
public interface SysUserQueryService extends IService<UserEntity> {

	/**
	 * 查询用户档案（基本信息、部门、岗位），含已禁用用户
	 */
	SysUserProfileResponse getProfile(Long userId);

	/**
	 * 按关键字搜索用户；仅返回未删除且状态为「正常」的用户。
	 * @param keyword 关键字；空或仅空白时返回空列表，不访问数据库
	 * @param limit 最大条数；{@code null} 时使用默认上限，超过绝对上限时截断
	 * @return 搜索结果，不含密码
	 */
	List<SysUserSearchItemResponse> searchByKeyword(String keyword, Integer limit);

}
