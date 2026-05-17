package com.auth.service.system.common.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.convert.SysUserProfileConverter;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.common.SystemCommonResultCode;
import com.auth.service.system.common.mapper.SysUserQueryMapper;
import com.auth.service.system.common.model.SysUserSearchItemResponse;
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
		if (userId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.ILLEGAL_USER_ID_NOT_NULL);
		}
		UserEntity user = baseMapper.selectById(userId);
		if (user == null) {
			throw new SystemBusinessException(SystemCommonResultCode.USER_NOT_FOUND, userId);
		}

		SysUserProfileResponse response = SysUserProfileConverter.INSTANCE.toProfileResponse(user);
		response.setDeptList(SysUserProfileConverter.INSTANCE.toDeptItemResponseList(
				Objects.requireNonNullElse(sysUserQueryMapper.selectDeptProfileByUserId(userId), List.of())));
		response.setPostList(SysUserProfileConverter.INSTANCE.toPostItemResponseList(
				Objects.requireNonNullElse(sysUserQueryMapper.selectPostProfileByUserId(userId), List.of())));

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

		return SysUserProfileConverter.INSTANCE.toSearchItemResponseList(Objects.requireNonNullElse(
				sysUserQueryMapper.searchByKeyword(keyword, limit == null ? 20 : Math.max(1, Math.min(limit, 50))),
				List.of()));
	}

}
