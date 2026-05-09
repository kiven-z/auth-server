package com.auth.service.system.common.service.impl;

import cn.hutool.core.util.StrUtil;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.admin.convert.SysUserProfileConverter;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.mapper.SysUserQueryMapper;
import com.auth.service.system.common.model.SysUserSearchItemResponse;
import com.auth.service.system.common.model.vo.SysUserDeptProfileItemResponse;
import com.auth.service.system.common.model.vo.SysUserPostProfileItemResponse;
import com.auth.service.system.common.model.vo.SysUserProfileResponse;
import com.auth.service.system.common.service.SysUserQueryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 系统用户只读查询实现
 *
 * @author Bunny
 */
@Service
public class SysUserQueryServiceImpl extends ServiceImpl<SysUserQueryMapper, UserEntity>
		implements SysUserQueryService {

	private static final int DEFAULT_SEARCH_LIMIT = 20;

	private static final int MAX_SEARCH_LIMIT = 50;

	private final SysUserQueryMapper sysUserQueryMapper;

	public SysUserQueryServiceImpl(SysUserQueryMapper sysUserQueryMapper) {
		this.sysUserQueryMapper = sysUserQueryMapper;
	}

	/**
	 * 将搜索条数限制在 [1, MAX_SEARCH_LIMIT]；null 时使用默认值。
	 */
	private static int resolveSearchLimit(Integer limit) {
		if (limit == null) {
			return DEFAULT_SEARCH_LIMIT;
		}
		if (limit < 1) {
			return 1;
		}
		return Math.min(limit, MAX_SEARCH_LIMIT);
	}

	/**
	 * 查询用户档案（基本信息、部门、岗位）
	 * @param userId 用户ID
	 * @return 聚合结果
	 */
	@Override
	public SysUserProfileResponse getProfile(Long userId) {
		if (userId == null) {
			throw new SystemBusinessException(SystemResultCodeEnum.ILLEGAL_USER_ID_NOT_NULL);
		}
		UserEntity user = baseMapper.selectById(userId);
		if (user == null) {
			throw new SystemBusinessException(SystemResultCodeEnum.USER_NOT_FOUND, userId);
		}

		SysUserProfileResponse response = SysUserProfileConverter.INSTANCE.toProfileResponse(user);

		List<SysUserDeptProfileItemResponse> deptList = sysUserQueryMapper.selectDeptProfileByUserId(userId);
		List<SysUserPostProfileItemResponse> postList = sysUserQueryMapper.selectPostProfileByUserId(userId);
		response.setDeptList(Objects.requireNonNullElse(deptList, List.of()));
		response.setPostList(Objects.requireNonNullElse(postList, List.of()));

		return response;
	}

	/**
	 * 按关键字搜索用户；仅返回未删除且状态为「正常」的用户。
	 * @param keyword 关键字；空或仅空白时返回空列表，不访问数据库
	 * @param limit 最大条数；{@code null} 时使用默认上限，超过绝对上限时截断
	 * @return 搜索结果，不含密码
	 */
	@Override
	public List<SysUserSearchItemResponse> searchByKeyword(String keyword, Integer limit) {
		if (!StringUtils.hasText(keyword)) {
			return Collections.emptyList();
		}
		String trimmed = StrUtil.trim(keyword);
		if (trimmed.isEmpty()) {
			return Collections.emptyList();
		}
		return sysUserQueryMapper.searchByKeyword(trimmed, resolveSearchLimit(limit));
	}

}
