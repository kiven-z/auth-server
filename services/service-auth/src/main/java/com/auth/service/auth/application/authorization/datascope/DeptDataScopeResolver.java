package com.auth.service.auth.application.authorization.datascope;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.api.datascope.DeptScopeMerger;
import com.auth.service.auth.application.port.authorization.DataScopeQueryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 部门维数据权限生效解析：user_scope 覆盖 role_scope，多角色宽松合并
 *
 * @author Bunny
 */
@Slf4j
@Service
public class DeptDataScopeResolver {

	private final DataScopeQueryPort dataScopeQueryPort;

	public DeptDataScopeResolver(DataScopeQueryPort dataScopeQueryPort) {
		this.dataScopeQueryPort = dataScopeQueryPort;
	}

	private static ScopeGrant selfGrant() {
		return ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build();
	}

	/**
	 * 解析用户的数据权限
	 * @param userId 用户ID
	 * @param roleCodes 角色码列表
	 * @return 数据权限
	 */
	public ScopeGrant resolveEffectiveGrant(Long userId, List<String> roleCodes) {
		if (userId == null) {
			return selfGrant();
		}

		var userScope = dataScopeQueryPort.findUserScopeByUserId(userId);
		if (userScope.isPresent()) {
			UserScopeRow row = userScope.get();
			ScopeGrant grant = materializeRow(row.scopeType(), row.scopeDeptIds());
			log.debug("user_scope 生效: userId={}, scopeType={}", userId, grant.getScopeType());
			return grant;
		}

		List<RoleScopeRow> roleScopes = dataScopeQueryPort.findRoleScopesByRoleCodes(roleCodes);
		if (CollUtil.isEmpty(roleScopes)) {
			log.debug("无 role_scope: userId={}, 默认 SELF", userId);
			return selfGrant();
		}

		List<ScopeGrant> grants = roleScopes.stream()
			.map(row -> materializeRow(row.scopeType(), row.scopeDeptIds()))
			.toList();
		ScopeGrant merged = DeptScopeMerger.mergeLoose(grants);
		if (merged == null) {
			log.debug("role_scope 合并为空: userId={}, 默认 SELF", userId);
			return selfGrant();
		}
		log.debug("role_scope 合并生效: userId={}, scopeType={}", userId, merged.getScopeType());
		return merged;
	}

	private ScopeGrant materializeRow(String scopeTypeRaw, String scopeDeptIdsRaw) {
		DataScopeStorageType storageType = DataScopeStorageType.parse(scopeTypeRaw);
		if (storageType == null) {
			return selfGrant();
		}
		List<Long> anchorDeptIds = JSONUtil.toList(scopeDeptIdsRaw, Long.class);
		List<Long> valuesForRedis = resolveValuesForRedis(storageType, anchorDeptIds);
		return switch (storageType) {
			case ALL -> ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build();
			case SELF -> selfGrant();
			case DEPT -> ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(valuesForRedis).build();
			case DEPT_AND_CHILD ->
				ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT_AND_CHILD).values(valuesForRedis).build();
			default -> selfGrant();
		};
	}

	private List<Long> resolveValuesForRedis(DataScopeStorageType storageType, List<Long> anchorDeptIds) {
		if (storageType != DataScopeStorageType.DEPT_AND_CHILD) {
			return anchorDeptIds;
		}
		if (CollUtil.isEmpty(anchorDeptIds)) {
			return List.of();
		}

		List<Long> expanded = dataScopeQueryPort.expandDescendantDeptIds(anchorDeptIds);
		if (CollUtil.isEmpty(expanded)) {
			return anchorDeptIds;
		}

		LinkedHashSet<Long> union = Stream.concat(anchorDeptIds.stream(), expanded.stream())
			.collect(Collectors.toCollection(LinkedHashSet::new));
		return List.copyOf(union);
	}

}
