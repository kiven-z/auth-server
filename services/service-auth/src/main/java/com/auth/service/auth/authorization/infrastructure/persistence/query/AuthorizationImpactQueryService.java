package com.auth.service.auth.authorization.infrastructure.persistence.query;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.auth.authorization.domain.invalidation.port.AuthorizationImpactQueryPort;
import com.auth.service.auth.authorization.infrastructure.persistence.AuthorizationImpactMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 授权影响面查询
 *
 * @author Bunny
 */
@Service
public class AuthorizationImpactQueryService implements AuthorizationImpactQueryPort {

	private final AuthorizationImpactMapper authorizationImpactMapper;

	public AuthorizationImpactQueryService(AuthorizationImpactMapper authorizationImpactMapper) {
		this.authorizationImpactMapper = authorizationImpactMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByRoleCodes(Collection<String> roleCodes) {
		List<String> codes = distinctNonBlankStrings(roleCodes);
		if (codes.isEmpty()) {
			return Set.of();
		}
		return toDistinctUserIdSet(authorizationImpactMapper.selectUserIdsByRoleCodes(codes));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByGrantSubjects(Collection<GrantSubjectKey> subjects) {
		if (CollUtil.isEmpty(subjects)) {
			return Set.of();
		}
		Set<Long> userIds = new HashSet<>();
		List<Long> userSubjectIds = new ArrayList<>();
		List<Long> deptSubjectIds = new ArrayList<>();
		List<Long> postSubjectIds = new ArrayList<>();
		for (GrantSubjectKey subject : subjects) {
			if (subject == null || subject.subjectType() == null || subject.subjectId() == null) {
				continue;
			}
			switch (subject.subjectType()) {
				case USER -> userSubjectIds.add(subject.subjectId());
				case DEPT -> deptSubjectIds.add(subject.subjectId());
				case POST -> postSubjectIds.add(subject.subjectId());
				default -> {
					// 规划中的主体类型尚未落地
				}
			}
		}
		if (CollUtil.isNotEmpty(userSubjectIds)) {
			userIds.addAll(toDistinctUserIdSet(
					authorizationImpactMapper.selectUserIdsByGrantUserSubjectIds(distinctLongs(userSubjectIds))));
		}
		if (CollUtil.isNotEmpty(deptSubjectIds)) {
			userIds.addAll(toDistinctUserIdSet(
					authorizationImpactMapper.selectUserIdsByGrantDeptSubjectIds(distinctLongs(deptSubjectIds))));
		}
		if (CollUtil.isNotEmpty(postSubjectIds)) {
			userIds.addAll(toDistinctUserIdSet(
					authorizationImpactMapper.selectUserIdsByGrantPostSubjectIds(distinctLongs(postSubjectIds))));
		}
		return Set.copyOf(userIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByDeptIds(Collection<Long> deptIds) {
		List<Long> ids = distinctLongs(deptIds);
		if (ids.isEmpty()) {
			return Set.of();
		}
		return toDistinctUserIdSet(authorizationImpactMapper.selectUserIdsByDeptIds(ids));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByPostIds(Collection<Long> postIds) {
		List<Long> ids = distinctLongs(postIds);
		if (ids.isEmpty()) {
			return Set.of();
		}
		return toDistinctUserIdSet(authorizationImpactMapper.selectUserIdsByPostIds(ids));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> findUserIdsByPermissionCodes(Collection<String> permissionCodes) {
		List<String> codes = distinctNonBlankStrings(permissionCodes);
		if (codes.isEmpty()) {
			return Set.of();
		}
		List<String> roleCodes = authorizationImpactMapper.selectRoleCodesByPermissionCodes(codes);
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
		List<String> codes = distinctNonBlankStrings(policyCodes);
		if (codes.isEmpty()) {
			return Set.of();
		}
		Set<Long> userIds = new HashSet<>();
		List<String> roleCodes = authorizationImpactMapper.selectRoleCodesByPolicyCodes(codes);
		if (CollUtil.isNotEmpty(roleCodes)) {
			userIds.addAll(findUserIdsByRoleCodes(roleCodes));
		}
		userIds.addAll(toDistinctUserIdSet(authorizationImpactMapper.selectUserIdsByPolicyBinding(codes)));
		return Set.copyOf(userIds);
	}

	private static List<String> distinctNonBlankStrings(Collection<String> values) {
		if (CollUtil.isEmpty(values)) {
			return List.of();
		}
		return values.stream().filter(CharSequenceUtil::isNotBlank).map(String::trim).distinct().toList();
	}

	private static List<Long> distinctLongs(Collection<Long> values) {
		if (CollUtil.isEmpty(values)) {
			return List.of();
		}
		return values.stream().filter(id -> id != null && id > 0).distinct().toList();
	}

	private static Set<Long> toDistinctUserIdSet(List<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return Set.of();
		}
		return userIds.stream().filter(id -> id != null && id > 0).collect(Collectors.toUnmodifiableSet());
	}

}
