package com.auth.service.auth.authorization.application.datascope;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.api.datascope.ScopeGrantMaterializer;
import com.auth.module.security.contract.api.granttable.ScopeGrantEffectiveMerger;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.service.auth.authorization.infrastructure.persistence.DeptClosureMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.RoleScopeMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.UserScopeMapper;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.RoleScopeProjection;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.UserScopeProjection;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

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
	 * 解析用户生效数据范围（单条 ScopeGrant，不含管理员短路）
	 * 
	 * @param userId    用户 ID
	 * @param roleCodes 用户角色码（来自 grant_table 展开）
	 * @return 生效范围；无配置时显式 {@link ScopeGrantType#SELF}
	 */
	public ScopeGrant resolveEffectiveGrant(Long userId, List<String> roleCodes) {
		if (userId == null) {
			return selfGrant();
		}

		UserScopeProjection userScope = userScopeMapper.selectByUserId(userId);
		if (userScope != null) {
			ScopeGrant grant = materializeRow(userScope.getScopeType(), userScope.getScopeDeptIds());
			log.debug("解析用户生效数据范围：userId={}, scopeType={}", userId, grant.getScopeType());
			return grant;
		}

		List<String> safeRoleCodes = roleCodes != null ? roleCodes : List.of();
		List<RoleScopeProjection> roleScopes = roleScopeMapper.selectByRoleCodes(safeRoleCodes);
		if (CollUtil.isEmpty(roleScopes)) {
			log.debug("无角色范围配置：userId={}, 默认 SELF", userId);
			return selfGrant();
		}

		List<ScopeGrant> grants = roleScopes.stream().map(this::materializeRoleScope).filter(Objects::nonNull).toList();
		ScopeGrant merged = ScopeGrantEffectiveMerger.mergeLoose(grants);
		if (merged == null) {
			log.debug("角色范围合并返回 null：userId={}, 默认 SELF", userId);
			return selfGrant();
		}
		log.debug("解析角色生效数据范围：userId={}, scopeType={}", userId, merged.getScopeType());
		return merged;
	}

	/**
	 * 物化角色范围
	 * 
	 * @param projection 角色范围投影
	 * @return 物化范围
	 */
	private ScopeGrant materializeRoleScope(RoleScopeProjection projection) {
		if (projection == null) {
			return null;
		}
		return materializeRow(projection.getScopeType(), projection.getScopeDeptIds());
	}

	/**
	 * 物化行范围
	 * 
	 * @param scopeTypeRaw    范围类型原始值
	 * @param scopeDeptIdsRaw 范围部门 ID 原始值
	 * @return 物化范围
	 */
	private ScopeGrant materializeRow(String scopeTypeRaw, String scopeDeptIdsRaw) {
		DataScopeStorageType storageType = DataScopeStorageType.parse(scopeTypeRaw);
		if (storageType == null) {
			return selfGrant();
		}
		List<String> anchorDeptIds = ScopeDeptIdsJsonParser.parse(scopeDeptIdsRaw);
		List<String> effectiveDeptIds = resolveDeptIdsForStorageType(storageType, anchorDeptIds);
		return ScopeGrantMaterializer.materialize(storageType, effectiveDeptIds);
	}

	/**
	 * 解析部门 ID 列表
	 * 
	 * @param storageType   存储类型
	 * @param anchorDeptIds 锚点部门 ID 列表
	 * @return 解析后的部门 ID 列表
	 */
	private List<String> resolveDeptIdsForStorageType(DataScopeStorageType storageType, List<String> anchorDeptIds) {
		if (storageType != DataScopeStorageType.DEPT_AND_CHILD) {
			return anchorDeptIds;
		}
		if (CollUtil.isEmpty(anchorDeptIds)) {
			return List.of();
		}
		List<Long> anchorLongIds = anchorDeptIds.stream().map(Long::valueOf).toList();
		List<Long> expanded = deptClosureMapper.selectDescendantDeptIdsByAncestorIds(anchorLongIds);
		if (CollUtil.isEmpty(expanded)) {
			return anchorDeptIds;
		}
		LinkedHashSet<String> union = new LinkedHashSet<>(anchorDeptIds);
		union.addAll(ScopeDeptIdsJsonParser.fromLongIds(expanded));
		return new ArrayList<>(union);
	}

	/**
	 * 仅本人范围
	 * 
	 * @return 仅本人范围
	 */
	private static ScopeGrant selfGrant() {
		return ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build();
	}

}
