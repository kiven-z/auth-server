package com.auth.service.auth.authorization.domain.datascope;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.service.auth.authorization.infrastructure.persistence.UserAuthorizationGrantMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.DataScopeProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户数据权限生效范围解析器
 *
 * <p>
 * 负责聚合 USER/ROLE/DEPT/POST... 授权片段并生成最终生效授权，供登录画像构建复用。
 * </p>
 *
 * @author Bunny
 */
@Slf4j
@Service
public class DataScopeEffectiveResolver {

	private final UserAuthorizationGrantMapper mapper;

	public DataScopeEffectiveResolver(UserAuthorizationGrantMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * 查询并构建用户生效数据权限
	 * @param userId 用户 ID
	 * @return 生效数据权限
	 */
	public Map<String, ScopeGrant> resolveByUserId(Long userId) {
		return resolveWithTrace(userId).getDataScopes();
	}

	/**
	 * 查询并构建用户生效数据权限（含轨迹）
	 * @param userId 用户 ID
	 * @return 生效数据权限构建结果
	 */
	public DataScopeBuildResult resolveWithTrace(Long userId) {
		if (userId == null) {
			return DataScopeBuildResult.builder().dataScopes(Map.of()).traces(List.of()).build();
		}
		List<DataScopeProjection> projections = mapper.selectEffectiveDataScopesByUserId(userId);

		// 查询出来结果不为空
		if (CollUtil.isEmpty(projections)) {
			DataScopeBuildResult emptyResult = DataScopeBuildResult.builder()
				.dataScopes(Map.of())
				.traces(List.of())
				.build();
			log.debug("Resolved data scopes for userId={}, scopeSize=0", userId);
			return emptyResult;
		}

		DataScopeBuildResult result = DataScopeBuilder.buildDataScopesWithTrace(projections);
		log.debug("Resolved data scopes for userId={}, scopeSize={}, traceSize={}", userId,
				result.getDataScopes().size(), result.getTraces().size());
		return result;
	}

}
