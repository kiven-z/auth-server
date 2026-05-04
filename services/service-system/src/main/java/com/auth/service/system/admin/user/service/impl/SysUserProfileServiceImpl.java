package com.auth.service.system.admin.user.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.admin.user.mapper.SysUserProfileMapper;
import com.auth.service.system.admin.user.model.vo.SysUserDeptProfileItemResponse;
import com.auth.service.system.admin.user.model.vo.SysUserPostProfileItemResponse;
import com.auth.service.system.admin.user.model.vo.SysUserProfileResponse;
import com.auth.service.system.admin.user.service.SysUserProfileService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.SystemResultCodeEnum;
import com.auth.service.system.common.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * 用户档案查询实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class SysUserProfileServiceImpl implements SysUserProfileService {

    private final UserMapper userMapper;

    private final SysUserProfileMapper sysUserProfileMapper;

    private static SysUserProfileResponse buildBaseProfile(UserEntity user) {
        SysUserProfileResponse vo = new SysUserProfileResponse();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setGender(user.getGender());
        vo.setBirthday(user.getBirthday());
        vo.setIntroduction(user.getIntroduction());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setLastLoginRegion(user.getLastLoginRegion());
        vo.setLastLoginTime(user.getLastLoginTime());
        return vo;
    }

    @Override
    public SysUserProfileResponse getProfile(Long userId) {
        if (userId == null) {
            throw new SystemBusinessException(SystemResultCodeEnum.ILLEGAL_USER_ID_NOT_NULL);
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new SystemBusinessException(SystemResultCodeEnum.USER_NOT_FOUND, userId);
        }

		// 构建基础用户信息
        SysUserProfileResponse response = buildBaseProfile(user);

        // 查询部门信息
        List<SysUserDeptProfileItemResponse> deptList = sysUserProfileMapper.selectDeptProfileByUserId(userId);
        // 查询岗位信息
        List<SysUserPostProfileItemResponse> postList = sysUserProfileMapper.selectPostProfileByUserId(userId);
        response.setDeptList(deptList);
        response.setPostList(postList);
        return response;
    }
}
