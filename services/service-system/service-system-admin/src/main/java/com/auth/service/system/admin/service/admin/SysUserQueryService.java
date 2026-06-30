package com.auth.service.system.admin.service.admin;

import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统用户只读查询（档案聚合、关键词搜索等）。
 *
 * @author Bunny
 */
public interface SysUserQueryService extends IService<UserEntity> {

	/**
	 * 用户分页查询
	 * @param pageParams 分页参数
	 * @param query 筛选条件
	 * @return 分页结果
	 */
	PageResponse<SysUserPageVO> getPage(Page<UserEntity> pageParams, SysUserPageQuery query);

	/**
	 * 查询用户档案（基本信息、部门、岗位），含已禁用用户
	 * @param userId 用户ID
	 * @return 用户档案
	 */
	SysUserProfileVO getProfile(Long userId);

	/**
	 * 查询用户详情（基本信息、审计字段、组织绑定与生效授权）
	 * @param userId 用户ID
	 * @return 用户详情
	 */
	SysUserDetailVO getDetail(Long userId);

	/**
	 * 按关键字搜索用户；仅返回未删除且状态为「正常」的用户。
	 * @param keyword 关键字；空或仅空白时返回空列表，不访问数据库
	 * @param limit 最大条数；{@code null} 时使用默认上限，超过绝对上限时截断
	 * @return 搜索结果，不含密码
	 */
	List<SysUserSearchItemVO> searchByKeyword(String keyword, Integer limit);

}
