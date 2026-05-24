package com.auth.service.auth.authorization.infrastructure.persistence.adapter;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.auth.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.authorization.domain.invalidation.port.AuthorizationImpactQueryPort;
import com.auth.service.auth.authorization.infrastructure.persistence.AuthorizationImpactMapper;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;

/**
 * 授权影响面查询 Port 适配器（MyBatis 实现）。
 *
 * @author Bunny
 */
@Repository
public class AuthorizationImpactQueryAdapter implements AuthorizationImpactQueryPort {

	private final AuthorizationImpactMapper authorizationImpactMapper;

	private final AuthorizationInvalidationProperties authorizationInvalidationProperties;

	public AuthorizationImpactQueryAdapter(AuthorizationImpactMapper authorizationImpactMapper,
			AuthorizationInvalidationProperties authorizationInvalidationProperties) {
		this.authorizationImpactMapper = authorizationImpactMapper;
		this.authorizationInvalidationProperties = authorizationInvalidationProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByRoleCodes(Collection<String> roleCodes) {
		List<String> codes = roleCodes.stream().distinct().toList();
		if (codes.isEmpty()) {
			return Set.of();
		}

		// 查询用户ID
		List<Long> list = authorizationImpactMapper.selectUserIdsByRoleCodes(codes);
		return Set.copyOf(list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByGrantSubjects(Collection<GrantSubjectKey> subjects) {
		Set<Long> userIds = new HashSet<>();
		List<Long> userSubjectIds = new ArrayList<>();
		List<Long> deptSubjectIds = new ArrayList<>();
		List<Long> postSubjectIds = new ArrayList<>();

		// 遍历授权主体集合，根据主体类型查询用户ID
		for (GrantSubjectKey subject : subjects) {
			switch (subject.subjectType()) {
				case USER -> userSubjectIds.add(subject.subjectId());
				case DEPT -> deptSubjectIds.add(subject.subjectId());
				case POST -> postSubjectIds.add(subject.subjectId());
				default -> {
					// 规划中的主体类型尚未落地
				}
			}
		}

		// 查询并累加用户ID
		queryAndAccumulate(userSubjectIds, authorizationImpactMapper::selectUserIdsByGrantUserSubjectIds, userIds);
		// 查询并累加用户ID
		queryAndAccumulate(deptSubjectIds, authorizationImpactMapper::selectUserIdsByGrantDeptSubjectIds, userIds);
		// 查询并累加用户ID
		queryAndAccumulate(postSubjectIds, authorizationImpactMapper::selectUserIdsByGrantPostSubjectIds, userIds);
		return Set.copyOf(userIds);
	}

	/**
	 * 分段查询并累加用户 ID，SQL 层已做 SELECT DISTINCT，返回值无需再次去重。
	 * @param subjectIds 授权主体ID集合
	 * @param queryFn 查询函数
	 * @param accumulator 累加器
	 */
	private void queryAndAccumulate(List<Long> subjectIds, Function<List<Long>, List<Long>> queryFn,
			Set<Long> accumulator) {
		if (CollUtil.isEmpty(subjectIds)) {
			return;
		}

		// 查询并累加用户ID
		List<Long> distinct = subjectIds.stream().distinct().toList();
		accumulator.addAll(queryFn.apply(distinct));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByDeptIds(Collection<Long> deptIds) {
		List<Long> ids = deptIds.stream().distinct().toList();
		if (ids.isEmpty()) {
			return Set.of();
		}

		// 查询用户ID
		List<Long> list = authorizationImpactMapper.selectUserIdsByDeptIds(ids);
		return Set.copyOf(list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByPostIds(Collection<Long> postIds) {
		List<Long> ids = postIds.stream().distinct().toList();
		if (ids.isEmpty()) {
			return Set.of();
		}

		// 查询用户ID
		List<Long> list = authorizationImpactMapper.selectUserIdsByPostIds(ids);
		return Set.copyOf(list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByPermissionCodes(Collection<String> permissionCodes) {
		List<String> codes = permissionCodes.stream().distinct().toList();

		// 查询角色代码
		List<String> roleCodes = authorizationImpactMapper.selectRoleCodesByPermissionCodes(codes);
		if (CollUtil.isEmpty(roleCodes)) {
			return Set.of();
		}

		// 查询用户ID
		return findUserIdsByRoleCodes(roleCodes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByPolicyCodes(Collection<String> policyCodes) {
		Set<Long> userIds = new HashSet<>();
		List<String> codes = policyCodes.stream().distinct().toList();
		if (codes.isEmpty()) {
			return Set.of();
		}

		// role_policy 未落地时仅走 user_access_policy_binding，避免 SQL 报错
		if (authorizationInvalidationProperties.isPolicyRoleBridgeEnabled()) {
			List<String> roleCodes = authorizationImpactMapper.selectRoleCodesByPolicyCodes(codes);
			if (CollUtil.isNotEmpty(roleCodes)) {
				userIds.addAll(findUserIdsByRoleCodes(roleCodes));
			}
		}

		List<Long> list = authorizationImpactMapper.selectUserIdsByPolicyBinding(codes);
		userIds.addAll(list);
		return Set.copyOf(userIds);
	}

}
