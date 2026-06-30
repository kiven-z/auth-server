package com.auth.service.auth.support.invalidation.impact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.mapper.AuthorizationImpactMapper;
import com.auth.service.auth.util.BatchPartition;

import org.springframework.stereotype.Repository;

/**
 * 授权影响面查询：按角色、权限、策略等维度反查受影响用户 ID。
 *
 * @author Bunny
 */
@Repository
public class AuthorizationImpactQuerySupport {

	private final AuthorizationImpactMapper authorizationImpactMapper;

	private final AuthorizationInvalidationProperties authorizationInvalidationProperties;

	public AuthorizationImpactQuerySupport(AuthorizationImpactMapper authorizationImpactMapper,
			AuthorizationInvalidationProperties authorizationInvalidationProperties) {
		this.authorizationImpactMapper = authorizationImpactMapper;
		this.authorizationInvalidationProperties = authorizationInvalidationProperties;
	}

	/**
	 * 按角色码反查所有可能持有该角色的用户 ID。
	 * @param roleCodes 角色编码
	 * @return 去重后的用户 ID 集合
	 */
	public Set<Long> findUserIdsByRoleCodes(Collection<String> roleCodes) {
		List<String> codes = roleCodes.stream().filter(Objects::nonNull).distinct().toList();
		if (codes.isEmpty()) {
			return Set.of();
		}

		Set<Long> userIds = new HashSet<>();
		queryStringKeysInBatches(codes, authorizationImpactMapper::selectUserIdsByRoleCodes, userIds);
		return Set.copyOf(userIds);
	}

	/**
	 * 按 grant_table 授权主体反查受影响用户 ID。
	 * @param subjects 授权主体键列表
	 * @return 去重后的用户 ID 集合
	 */
	public Set<Long> findUserIdsByGrantSubjects(Collection<GrantSubjectKey> subjects) {
		Set<Long> userIds = new HashSet<>();
		List<Long> userSubjectIds = new ArrayList<>();
		List<Long> deptSubjectIds = new ArrayList<>();
		List<Long> postSubjectIds = new ArrayList<>();

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

		queryLongKeysInBatches(userSubjectIds, authorizationImpactMapper::selectUserIdsByGrantUserSubjectIds, userIds);
		queryLongKeysInBatches(deptSubjectIds, authorizationImpactMapper::selectUserIdsByGrantDeptSubjectIds, userIds);
		queryLongKeysInBatches(postSubjectIds, authorizationImpactMapper::selectUserIdsByGrantPostSubjectIds, userIds);
		return Set.copyOf(userIds);
	}

	/**
	 * 按权限码反查受影响用户 ID。
	 * @param permissionCodes 权限码
	 * @return 去重后的用户 ID 集合
	 */
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
	 * 按策略码反查受影响用户 ID。
	 * @param policyCodes 策略码
	 * @return 去重后的用户 ID 集合
	 */
	public Set<Long> findUserIdsByPolicyCodes(Collection<String> policyCodes) {
		Set<Long> userIds = new HashSet<>();
		List<String> codes = policyCodes.stream().filter(Objects::nonNull).distinct().toList();
		if (codes.isEmpty()) {
			return Set.of();
		}

		if (authorizationInvalidationProperties.isPolicyRoleBridgeEnabled()) {
			Set<String> roleCodes = new LinkedHashSet<>();
			accumulateBridgedStringsInBatches(codes, authorizationImpactMapper::selectRoleCodesByPolicyCodes,
					roleCodes);
			if (CollUtil.isNotEmpty(roleCodes)) {
				userIds.addAll(findUserIdsByRoleCodes(roleCodes));
			}
		}

		queryStringKeysInBatches(codes, authorizationImpactMapper::selectUserIdsByPolicyBinding, userIds);
		return Set.copyOf(userIds);
	}

	/**
	 * 按 Long 型键分批反查用户 ID 并去重。
	 * @param keys 查询键
	 * @param queryFn 分批查询函数
	 * @return 去重后的用户 ID 集合
	 */
	public Set<Long> findUserIdsByLongKeys(Collection<Long> keys, UnaryOperator<List<Long>> queryFn) {
		List<Long> ids = keys.stream().filter(Objects::nonNull).distinct().toList();
		if (ids.isEmpty()) {
			return Set.of();
		}

		Set<Long> userIds = new HashSet<>();
		queryLongKeysInBatches(ids, queryFn, userIds);
		return Set.copyOf(userIds);
	}

	private void queryLongKeysInBatches(List<Long> keys, UnaryOperator<List<Long>> queryFn, Set<Long> accumulator) {
		int batchSize = authorizationInvalidationProperties.getBatchSize();

		for (List<Long> chunk : BatchPartition.partitionIds(keys, batchSize)) {
			accumulator.addAll(queryFn.apply(chunk));
		}
	}

	private void queryStringKeysInBatches(List<String> keys, Function<List<String>, List<Long>> queryFn,
			Set<Long> accumulator) {
		int batchSize = authorizationInvalidationProperties.getBatchSize();

		for (List<String> chunk : BatchPartition.partitionStrings(keys, batchSize)) {
			accumulator.addAll(queryFn.apply(chunk));
		}
	}

	private void accumulateBridgedStringsInBatches(List<String> keys, UnaryOperator<List<String>> queryFn,
			Set<String> accumulator) {
		int batchSize = authorizationInvalidationProperties.getBatchSize();

		for (List<String> chunk : BatchPartition.partitionStrings(keys, batchSize)) {
			accumulator.addAll(queryFn.apply(chunk));
		}
	}

}
