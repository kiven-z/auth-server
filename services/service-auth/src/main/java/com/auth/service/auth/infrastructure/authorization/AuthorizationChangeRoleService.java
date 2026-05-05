package com.auth.service.auth.infrastructure.authorization;

import com.auth.service.auth.mapper.AuthorizationImpactMapper;
import com.auth.service.auth.mapper.UserRolePermissionMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色变更主体修改
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AuthorizationChangeRoleService {
    private final UserRolePermissionMapper userRolePermissionMapper;

    private final AuthorizationImpactMapper authorizationImpactMapper;

    /**
     * 查询持有指定角色的所有用户 ID（通过 USER / DEPT / POST 三类主体汇总）
     *
     * @param roleCode 角色编码
     * @return 用户 ID 列表
     */
    public List<Long> listUserIdsByRoleCode(String roleCode) {
        return authorizationImpactMapper.selectUserIdsByRoleCode(roleCode);
    }
}
