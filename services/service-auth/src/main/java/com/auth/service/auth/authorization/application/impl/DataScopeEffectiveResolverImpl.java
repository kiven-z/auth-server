package com.auth.service.auth.authorization.application.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.service.auth.authorization.application.DataScopeEffectiveResolver;
import com.auth.service.auth.authorization.domain.datascope.DataScopeBuildResult;
import com.auth.service.auth.authorization.domain.datascope.DataScopeBuilder;
import com.auth.service.auth.authorization.infrastructure.persistence.UserAuthorizationGrantMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.DataScopeProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户数据权限生效范围解析实现：查询授权片段后委托领域构建器合并。
 *
 * @author Bunny
 */
@Slf4j
@Service
public class DataScopeEffectiveResolverImpl implements DataScopeEffectiveResolver {

	private final UserAuthorizationGrantMapper mapper;

	public DataScopeEffectiveResolverImpl(UserAuthorizationGrantMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, ScopeGrant> resolveByUserId(Long userId) {
		return resolveWithTrace(userId).getDataScopes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataScopeBuildResult resolveWithTrace(Long userId) {
		if (userId == null) {
			return DataScopeBuildResult.builder().dataScopes(Map.of()).traces(List.of()).build();
		}
		List<DataScopeProjection> projections = mapper.selectEffectiveDataScopesByUserId(userId);

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
