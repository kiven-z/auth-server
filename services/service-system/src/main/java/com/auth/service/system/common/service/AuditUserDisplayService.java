package com.auth.service.system.common.service;

import java.util.Collection;
import java.util.Map;

import com.auth.common.core.model.response.BaseResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 审计用户展示名服务：将创建人 / 更新人用户 ID 解析为用户名，供列表与跨服务调用复用。
 *
 * @author Bunny
 */
public interface AuditUserDisplayService {

	/**
	 * 根据用户主键批量查询用户名（去重后一次查询，避免 N+1）。
	 * @param userIds 用户主键集合；为 {@code null} 或空时返回空 Map
	 * @return 用户 ID 到用户名的映射；不存在的用户不会出现在 Map 中
	 */
	Map<Long, String> mapUsernamesByIds(Collection<Long> userIds);

	/**
	 * 为分页结果中的每条 {@link BaseResponse} 填充 {@code createUsername}、{@code updateUsername}。
	 * @param page MyBatis-Plus 分页结果
	 * @param <T> 继承 {@link BaseResponse} 的行类型
	 */
	<T extends BaseResponse> void enrichAuditUsernames(IPage<T> page);

}
