package com.auth.service.system.admin.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.convert.SysUserProfileConverter;
import com.auth.service.system.admin.mapper.SysUserQueryMapper;
import com.auth.service.system.admin.model.SysUserSearchItemResponse;
import com.auth.service.system.admin.model.vo.sysuser.SysUserProfileResponse;
import com.auth.service.system.admin.service.SysUserQueryService;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 系统用户只读查询实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysUserQueryServiceImpl extends ServiceImpl<SysUserQueryMapper, UserEntity>
		implements SysUserQueryService {

	private final SysUserQueryMapper sysUserQueryMapper;

	private final UserReferenceChecker userReferenceChecker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysUserProfileResponse getProfile(Long userId) {
		if (userId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.ILLEGAL_USER_ID_NOT_NULL);
		}
		UserEntity user = userReferenceChecker.getExistingActive(userId);

		SysUserProfileResponse response = SysUserProfileConverter.INSTANCE.toProfileResponse(user);
		response.setDeptList(SysUserProfileConverter.INSTANCE
			.toDeptItemResponseList(sysUserQueryMapper.selectDeptProfileByUserId(userId)));
		response.setPostList(SysUserProfileConverter.INSTANCE
			.toPostItemResponseList(sysUserQueryMapper.selectPostProfileByUserId(userId)));

		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysUserSearchItemResponse> searchByKeyword(String keyword, Integer limit) {
		if (CharSequenceUtil.isBlank(keyword)) {
			return Collections.emptyList();
		}

		return SysUserProfileConverter.INSTANCE.toSearchItemResponseList(Objects.requireNonNullElse(
				sysUserQueryMapper.searchByKeyword(keyword, limit == null ? 20 : Math.max(1, Math.min(limit, 50))),
				List.of()));
	}

}
