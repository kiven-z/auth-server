package com.auth.service.auth.support.authorization;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.api.datascope.DeptScopeMerger;
import com.auth.service.auth.mapper.DeptClosureMapper;
import com.auth.service.auth.mapper.RoleScopeMapper;
import com.auth.service.auth.mapper.UserScopeMapper;
import com.auth.service.auth.model.po.scope.RoleScopePO;
import com.auth.service.auth.model.po.scope.UserScopePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 部门维数据权限生效解析：user_scope 覆盖 role_scope，多角色宽松合并
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class DeptDataScopeResolver {

	private final UserScopeMapper userScopeMapper;

	private final RoleScopeMapper roleScopeMapper;

	private final DeptClosureMapper deptClosureMapper;

	/**
	 * 生成 SELF 数据权限
	 * @return SELF 数据权限
	 */
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

		Optional<UserScopePO> userScope = Optional.ofNullable(userScopeMapper.selectByUserId(userId));
		if (userScope.isPresent()) {
			UserScopePO scope = userScope.get();
			ScopeGrant grant = materializeRow(scope.getScopeType(), scope.getScopeDeptIds());
			log.debug("user_scope 生效: userId={}, scopeType={}", userId, grant.getScopeType());
			return grant;
		}

		List<RoleScopePO> roleScopes = roleScopeMapper.selectByRoleCodes(roleCodes);
		if (CollUtil.isEmpty(roleScopes)) {
			log.debug("无 role_scope: userId={}, 默认 SELF", userId);
			return selfGrant();
		}

		List<ScopeGrant> grants = roleScopes.stream()
			.map(scope -> materializeRow(scope.getScopeType(), scope.getScopeDeptIds()))
			.toList();
		ScopeGrant merged = DeptScopeMerger.mergeLoose(grants);
		if (merged == null) {
			log.debug("role_scope 合并为空: userId={}, 默认 SELF", userId);
			return selfGrant();
		}
		log.debug("role_scope 合并生效: userId={}, scopeType={}", userId, merged.getScopeType());
		return merged;
	}

	/**
	 * 将数据库中的数据转换为 ScopeGrant 对象
	 * @param scopeTypeRaw 数据库中的 scope_type 字段
	 * @param scopeDeptIdsRaw 数据库中的 scope_dept_ids 字段
	 * @return ScopeGrant 对象
	 */
	private ScopeGrant materializeRow(String scopeTypeRaw, String scopeDeptIdsRaw) {
		DataScopeStorageType storageType = DataScopeStorageType.parse(scopeTypeRaw);
		if (storageType == null) {
			return selfGrant();
		}

		List<Long> anchorDeptIds = JSONUtil.toList(scopeDeptIdsRaw, Long.class);
		List<Long> valuesForRedis = resolveValuesForRedis(storageType, anchorDeptIds);
		return switch (storageType) {
			case ALL -> ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build();
			case DEPT -> ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(valuesForRedis).build();
			case DEPT_AND_CHILD ->
				ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT_AND_CHILD).values(valuesForRedis).build();
			default -> selfGrant();
		};
	}

	/**
	 * 将数据库中的 scope_dept_ids 字段转换为 Long 列表
	 * @param storageType 数据范围存储类型
	 * @param anchorDeptIds 数据库中的 scope_dept_ids 字段
	 * @return Long 列表
	 */
	private List<Long> resolveValuesForRedis(DataScopeStorageType storageType, List<Long> anchorDeptIds) {
		if (storageType != DataScopeStorageType.DEPT_AND_CHILD) {
			return anchorDeptIds;
		}

		if (CollUtil.isEmpty(anchorDeptIds)) {
			return List.of();
		}

		List<Long> expanded = deptClosureMapper.selectDescendantDeptIdsByAncestorIds(anchorDeptIds);
		if (CollUtil.isEmpty(expanded)) {
			return anchorDeptIds;
		}

		LinkedHashSet<Long> union = Stream.concat(anchorDeptIds.stream(), expanded.stream())
			.collect(Collectors.toCollection(LinkedHashSet::new));
		return List.copyOf(union);
	}

}
