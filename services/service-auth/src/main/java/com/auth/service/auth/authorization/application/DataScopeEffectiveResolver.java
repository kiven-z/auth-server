package com.auth.service.auth.authorization.application;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.service.auth.authorization.domain.datascope.DataScopeBuildResult;
import com.auth.service.auth.authorization.domain.datascope.DataScopeBuilder;
import com.auth.service.auth.authorization.domain.datascope.DataScopeGrantPiece;
import com.auth.service.auth.authorization.infrastructure.persistence.UserAuthorizationGrantMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.DataScopeProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户数据权限生效范围解析：聚合授权片段并生成最终生效范围
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
	 * 将持久层投影转为领域读模型，避免领域构建器依赖基础设施类型。
	 * @param projections Mapper 查询结果
	 * @return 授权片段列表
	 */
	private static List<DataScopeGrantPiece> toGrantPieces(List<DataScopeProjection> projections) {
		return projections.stream()
			.filter(Objects::nonNull)
			.map(p -> new DataScopeGrantPiece(p.getSubjectType(), p.getScopeResourceCode(), p.getDimensionType(),
					p.getScopeType(), p.getScopeSubjectIds()))
			.toList();
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

		// 查询用户有效数据权限
		List<DataScopeProjection> projections = mapper.selectEffectiveDataScopesByUserId(userId);
		if (CollUtil.isEmpty(projections)) {
			DataScopeBuildResult emptyResult = DataScopeBuildResult.builder()
				.dataScopes(Map.of())
				.traces(List.of())
				.build();
			log.debug("Resolved data scopes for userId={}, scopeSize=0", userId);
			return emptyResult;
		}

		// 将持久层投影转为领域读模型，避免领域构建器依赖基础设施类型
		List<DataScopeGrantPiece> grantPieces = toGrantPieces(projections);
		DataScopeBuildResult result = DataScopeBuilder.buildDataScopesWithTrace(grantPieces);
		log.debug("Resolved data scopes for userId={}, scopeSize={}, traceSize={}", userId,
				result.getDataScopes().size(), result.getTraces().size());
		return result;
	}

}
