package com.auth.service.system.common.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
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

	private final SysUserQueryMapper sysUserQueryMapper;

	public SysUserQueryServiceImpl(SysUserQueryMapper sysUserQueryMapper) {
		this.sysUserQueryMapper = sysUserQueryMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysUserProfileResponse getProfile(Long userId) {
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
	 * {@inheritDoc}
	 */
	@Override
	public List<SysUserSearchItemResponse> searchByKeyword(String keyword, Integer limit) {
		keyword = CharSequenceUtil.trim(keyword);
		if (CharSequenceUtil.isEmpty(keyword)) {
			return Collections.emptyList();
		}

		return sysUserQueryMapper.searchByKeyword(keyword, limit == null ? 20 : Math.max(1, Math.min(limit, 50)));
	}

}
