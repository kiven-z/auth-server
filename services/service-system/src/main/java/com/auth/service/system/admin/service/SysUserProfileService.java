package com.auth.service.system.admin.service;

import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.system.admin.vo.SysUserProfileResponse;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户档案查询
 *
 * @author Bunny
 */
public interface SysUserProfileService extends IService<UserEntity> {

    /**
     * 查询用户档案（基本信息、部门、岗位）
     *
     * @param userId 用户ID
     * @return 聚合结果
     */
    SysUserProfileResponse getProfile(Long userId);

}
