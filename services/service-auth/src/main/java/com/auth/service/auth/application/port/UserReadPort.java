package com.auth.service.auth.application.port;

import com.auth.service.auth.domain.authorization.model.UserAccountSnapshot;
import com.auth.service.auth.domain.session.model.UserOnlineBrief;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 用户只读端口：供授权上下文与在线列表加载用户主数据，由认证基础设施实现
 *
 * @author Bunny
 */
public interface UserReadPort {

	/**
	 * 按用户 ID 查询授权用用户快照
	 * @param userId 用户 ID
	 * @return 用户快照；不存在时为空
	 */
	Optional<UserAccountSnapshot> findById(Long userId);

	/**
	 * 按用户 ID 批量查询在线列表展示快照
	 * @param userIds 用户 ID 集合
	 * @return 用户 ID 到展示快照的映射
	 */
	Map<Long, UserOnlineBrief> findOnlineBriefByIds(Collection<Long> userIds);

}
