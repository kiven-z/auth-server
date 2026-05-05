package com.auth.service.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限信息反查
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationImpactMapper {
    /**
     * 查询持有指定角色的所有用户 ID（通过 USER / DEPT / POST 三类主体汇总）
     *
     * @param roleCode 角色编码
     * @return 用户 ID 列表
     */
    List<Long> selectUserIdsByRoleCode(@Param("roleCode") String roleCode);
}
