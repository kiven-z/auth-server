package com.auth.service.auth.internal.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 授权影响面：按角色/权限/策略/grant 关联维度反查受影响用户 ID（与 UserRolesCte 同源）
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationImpactMapper {

}
