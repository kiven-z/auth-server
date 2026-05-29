package com.auth.service.auth.authorization.application.datascope;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.api.granttable.ScopeGrantEffectiveMerger;
import com.auth.service.auth.authorization.infrastructure.persistence.DeptClosureMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.RoleScopeMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.UserScopeMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.RoleScopeProjection;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.UserScopeProjection;
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

	private final UserScopeMapper userScopeMapper;

	private final RoleScopeMapper roleScopeMapper;

	private final DeptClosureMapper deptClosureMapper;

	public DeptDataScopeResolver(UserScopeMapper userScopeMapper, RoleScopeMapper roleScopeMapper,
			DeptClosureMapper deptClosureMapper) {
		this.userScopeMapper = userScopeMapper;
		this.roleScopeMapper = roleScopeMapper;
		this.deptClosureMapper = deptClosureMapper;
	}

	/**
	 * 默认数据权限
	 * @return 数据权限
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

		// 有 user_scope 则不再读 role_scope
		UserScopeProjection userScope = userScopeMapper.selectByUserId(userId);
		if (userScope != null) {
			ScopeGrant grant = materializeRow(userScope.getScopeType(), userScope.getScopeDeptIds());
			log.debug("user_scope 生效: userId={}, scopeType={}", userId, grant.getScopeType());
			return grant;
		}

		List<RoleScopeProjection> roleScopes = roleScopeMapper.selectByRoleCodes(roleCodes);
		if (CollUtil.isEmpty(roleScopes)) {
			log.debug("无 role_scope: userId={}, 默认 SELF", userId);
			return selfGrant();
		}

		List<ScopeGrant> grants = roleScopes.stream()
			.map(projection -> materializeRow(projection.getScopeType(), projection.getScopeDeptIds()))
			.toList();
		ScopeGrant merged = ScopeGrantEffectiveMerger.mergeLoose(grants);
		if (merged == null) {
			log.debug("role_scope 合并为空: userId={}, 默认 SELF", userId);
			return selfGrant();
		}
		log.debug("role_scope 合并生效: userId={}, scopeType={}", userId, merged.getScopeType());
		return merged;
	}

	/**
	 * 将数据库中的数据权限转换为ScopeGrant对象
	 * @param scopeTypeRaw 数据权限类型
	 * @param scopeDeptIdsRaw 数据权限部门ID列表
	 * @return ScopeGrant对象
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
			case SELF -> selfGrant();
			case DEPT -> ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(valuesForRedis).build();
			case DEPT_AND_CHILD ->
				ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT_AND_CHILD).values(valuesForRedis).build();
			default -> selfGrant();
		};
	}

	/**
	 * DEPT 存锚点；DEPT_AND_CHILD 登录时经 dept_closure 展开后写入 values
	 * @param storageType 数据权限类型
	 * @param anchorDeptIds 锚点部门ID列表
	 * @return 部门ID列表
	 */
	private List<Long> resolveValuesForRedis(DataScopeStorageType storageType, List<Long> anchorDeptIds) {
		if (storageType != DataScopeStorageType.DEPT_AND_CHILD) {
			return anchorDeptIds;
		}
		if (CollUtil.isEmpty(anchorDeptIds)) {
			return List.of();
		}

		// 按照部门展开
		List<Long> expanded = deptClosureMapper.selectDescendantDeptIdsByAncestorIds(anchorDeptIds);
		if (CollUtil.isEmpty(expanded)) {
			return anchorDeptIds;
		}

		LinkedHashSet<Long> union = Stream.concat(anchorDeptIds.stream(), expanded.stream())
			.collect(Collectors.toCollection(LinkedHashSet::new));
		return List.copyOf(union);
	}

}
