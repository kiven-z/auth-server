package com.auth.service.auth.identity.application;

import com.auth.module.platform.persistence.model.UserEntity;

/**
 * 用户主数据只读网关：供跨 BC 查询用户基础信息，避免直接依赖认证 BC 的 Mapper。
 *
 * @author Bunny
 */
public interface UserReadGateway {

	/**
	 * 按主键查询用户（含已删除记录，由调用方决定是否校验）。
	 * @param userId 用户 ID
	 * @return 用户实体；不存在时返回 null
	 */
	UserEntity findById(Long userId);

}
