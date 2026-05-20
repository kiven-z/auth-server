package com.auth.service.auth.authorization.domain.port;

import com.auth.service.auth.authorization.domain.model.UserAccountSnapshot;

import java.util.Optional;

/**
 * 用户只读端口：供授权上下文加载用户主数据，由认证基础设施实现。
 *
 * @author Bunny
 */
public interface UserReadPort {

	/**
	 * 按用户 ID 查询授权用用户快照。
	 * @param userId 用户 ID
	 * @return 用户快照；不存在时为空
	 */
	Optional<UserAccountSnapshot> findById(Long userId);

}
