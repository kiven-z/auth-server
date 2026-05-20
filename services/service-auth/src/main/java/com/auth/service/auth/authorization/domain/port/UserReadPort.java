package com.auth.service.auth.authorization.domain.port;

import com.auth.module.platform.persistence.model.UserEntity;

import java.util.Optional;

/**
 * 用户只读端口：供授权上下文加载用户主数据，由认证基础设施实现。
 *
 * @author Bunny
 */
public interface UserReadPort {

	/**
	 * 按用户 ID 查询用户实体。
	 * @param userId 用户 ID
	 * @return 用户实体；不存在时为空
	 */
	Optional<UserEntity> findById(Long userId);

}
