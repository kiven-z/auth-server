package com.auth.service.auth.infrastructure.persistence.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Repository;

import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.auth.application.port.invalidation.AuthorizationImpactQueryPort;
import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.infrastructure.persistence.mapper.AuthorizationImpactMapper;
import com.auth.service.auth.shared.support.BatchPartition;

import cn.hutool.core.collection.CollUtil;

/**
 * 授权影响面查询 Port 适配器
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
		List<String> codes = roleCodes.stream().filter(Objects::nonNull).distinct().toList();
		if (codes.isEmpty()) {
			return Set.of();
		}

		// 查询用户ID
		Set<Long> userIds = new HashSet<>();
		queryStringKeysInBatches(codes, authorizationImpactMapper::selectUserIdsByRoleCodes, userIds);
		return Set.copyOf(userIds);
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
		queryLongKeysInBatches(userSubjectIds, authorizationImpactMapper::selectUserIdsByGrantUserSubjectIds, userIds);
		// 查询并累加用户ID
		queryLongKeysInBatches(deptSubjectIds, authorizationImpactMapper::selectUserIdsByGrantDeptSubjectIds, userIds);
		// 查询并累加用户ID
		queryLongKeysInBatches(postSubjectIds, authorizationImpactMapper::selectUserIdsByGrantPostSubjectIds, userIds);
		return Set.copyOf(userIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByDeptIds(Collection<Long> deptIds) {
		return findUserIdsByLongKeys(deptIds, authorizationImpactMapper::selectUserIdsByDeptIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByPostIds(Collection<Long> postIds) {
		return findUserIdsByLongKeys(postIds, authorizationImpactMapper::selectUserIdsByPostIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByPermissionCodes(Collection<String> permissionCodes) {
		List<String> codes = permissionCodes.stream().filter(Objects::nonNull).distinct().toList();
		if (codes.isEmpty()) {
			return Set.of();
		}

		Set<String> roleCodes = new LinkedHashSet<>();
		accumulateBridgedStringsInBatches(codes, authorizationImpactMapper::selectRoleCodesByPermissionCodes,
				roleCodes);
		if (CollUtil.isEmpty(roleCodes)) {
			return Set.of();
		}

		return findUserIdsByRoleCodes(roleCodes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByPolicyCodes(Collection<String> policyCodes) {
		Set<Long> userIds = new HashSet<>();
		List<String> codes = policyCodes.stream().filter(Objects::nonNull).distinct().toList();
		if (codes.isEmpty()) {
			return Set.of();
		}

		// 如果策略码桥接开启，则查询角色码并累加用户ID
		if (authorizationInvalidationProperties.isPolicyRoleBridgeEnabled()) {
			Set<String> roleCodes = new LinkedHashSet<>();
			accumulateBridgedStringsInBatches(codes, authorizationImpactMapper::selectRoleCodesByPolicyCodes,
					roleCodes);
			if (CollUtil.isNotEmpty(roleCodes)) {
				userIds.addAll(findUserIdsByRoleCodes(roleCodes));
			}
		}

		// 查询并累加用户ID
		queryStringKeysInBatches(codes, authorizationImpactMapper::selectUserIdsByPolicyBinding, userIds);
		return Set.copyOf(userIds);
	}

	/**
	 * Long 键去重归一化后分片反查用户 ID
	 */
	private Set<Long> findUserIdsByLongKeys(Collection<Long> keys, UnaryOperator<List<Long>> queryFn) {
		List<Long> ids = keys.stream().filter(Objects::nonNull).distinct().toList();
		if (ids.isEmpty()) {
			return Set.of();
		}

		// 查询用户ID
		Set<Long> userIds = new HashSet<>();
		queryLongKeysInBatches(ids, queryFn, userIds);
		return Set.copyOf(userIds);
	}

	/**
	 * Long 键分片查询并累加用户 ID
	 * @param keys 键列表
	 * @param queryFn 查询函数
	 * @param accumulator 累加器
	 */
	private void queryLongKeysInBatches(List<Long> keys, UnaryOperator<List<Long>> queryFn, Set<Long> accumulator) {
		int batchSize = authorizationInvalidationProperties.getBatchSize();

		for (List<Long> chunk : BatchPartition.partitionIds(keys, batchSize)) {
			accumulator.addAll(queryFn.apply(chunk));
		}
	}

	/**
	 * String 键分片查询并累加用户 ID。
	 * @param keys 键列表
	 * @param queryFn 查询函数
	 * @param accumulator 累加器
	 */
	private void queryStringKeysInBatches(List<String> keys, Function<List<String>, List<Long>> queryFn,
			Set<Long> accumulator) {
		int batchSize = authorizationInvalidationProperties.getBatchSize();

		for (List<String> chunk : BatchPartition.partitionStrings(keys, batchSize)) {
			accumulator.addAll(queryFn.apply(chunk));
		}
	}

	/**
	 * String 键分片查询并累加桥接结果（如权限码 → 角色码）。
	 * @param keys 键列表
	 * @param queryFn 查询函数
	 * @param accumulator 累加器
	 */
	private void accumulateBridgedStringsInBatches(List<String> keys, UnaryOperator<List<String>> queryFn,
			Set<String> accumulator) {
		int batchSize = authorizationInvalidationProperties.getBatchSize();

		for (List<String> chunk : BatchPartition.partitionStrings(keys, batchSize)) {
			accumulator.addAll(queryFn.apply(chunk));
		}
	}

}
