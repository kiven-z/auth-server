package com.auth.service.system.admin.user.service.impl;

import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.admin.user.convert.SysUserProfileConverter;
import com.auth.service.system.admin.user.mapper.SysUserProfileMapper;
import com.auth.service.system.admin.user.model.vo.SysUserDeptProfileItemResponse;
import com.auth.service.system.admin.user.model.vo.SysUserPostProfileItemResponse;
import com.auth.service.system.admin.user.model.vo.SysUserProfileResponse;
import com.auth.service.system.admin.user.service.SysUserProfileService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 用户档案查询实现
 *
 * @author Bunny
 */
@Service
public class SysUserProfileServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements SysUserProfileService {

	private final SysUserProfileMapper sysUserProfileMapper;

	public SysUserProfileServiceImpl(SysUserProfileMapper sysUserProfileMapper) {
		this.sysUserProfileMapper = sysUserProfileMapper;
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

		// 构建基础用户信息
		SysUserProfileResponse response = SysUserProfileConverter.INSTANCE.toProfileResponse(user);

		// 查询部门信息
		List<SysUserDeptProfileItemResponse> deptList = sysUserProfileMapper.selectDeptProfileByUserId(userId);
		// 查询岗位信息
		List<SysUserPostProfileItemResponse> postList = sysUserProfileMapper.selectPostProfileByUserId(userId);
		response.setDeptList(Objects.requireNonNullElse(deptList, List.of()));
		response.setPostList(Objects.requireNonNullElse(postList, List.of()));

		return response;
	}

}
