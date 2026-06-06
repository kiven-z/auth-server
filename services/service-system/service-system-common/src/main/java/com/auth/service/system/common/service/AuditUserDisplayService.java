package com.auth.service.system.common.service;

import com.auth.common.core.model.response.BaseResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 审计用户展示名服务：将创建人 / 更新人用户 ID 解析为用户名，供列表与跨服务调用复用。
 *
 * @author Bunny
 */
public interface AuditUserDisplayService {

	/**
	 * 批量解析用户名（含已禁用、已删除用户；一次查询）
	 * @param userIds 为 null 或空时返回空 Map
	 * @return 仅包含能解析到的 id → username
	 */
	Map<Long, String> mapUsernamesByIds(Collection<Long> userIds);

	/**
	 * 为分页结果中的每条 {@link BaseResponse} 填充 {@code createUsername}、{@code updateUsername}。
	 * @param page MyBatis-Plus 分页结果
	 * @param <T> 继承 {@link BaseResponse} 的行类型
	 */
	<T extends BaseResponse> void enrichAuditUsernames(IPage<T> page);

	/**
	 * 为列表中的每条 {@link BaseResponse} 填充
	 * {@code createUsername}、{@code updateUsername}（与分页查询同源逻辑）。
	 * @param records 行列表；为 {@code null} 或空时不处理
	 */
	void enrichAuditUsernames(List<? extends BaseResponse> records);

}
