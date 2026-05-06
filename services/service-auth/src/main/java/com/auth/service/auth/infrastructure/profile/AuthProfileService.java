package com.auth.service.auth.infrastructure.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.PolicyBundle;
import com.auth.module.security.contract.api.authorization.PolicyRule;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.DataScopeGrantTableSubjectType;
import com.auth.module.security.contract.api.granttable.ScopeGrantEffectiveMerger;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.mapper.UserRolePermissionDataScopePolicyMapper;
import com.auth.service.auth.model.projection.DataScopeProjection;
import com.auth.service.auth.model.projection.PolicyProjection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * 只做 AuthProfile 构建 创建认证授权画像，授权画像构建服务实现
 *
 * @author Bunny
 */
@Service
public class AuthProfileService {

	private static final TypeReference<List<Object>> LIST_OBJECT_TYPE = new TypeReference<>() {
	};

	private static final TypeReference<Map<String, Object>> MAP_OBJECT_TYPE = new TypeReference<>() {
	};

	private final UserRolePermissionDataScopePolicyMapper userRolePermissionDataScopePolicyMapper;

	private final ObjectMapper objectMapper;

	private final UserMapper userMapper;

	public AuthProfileService(UserRolePermissionDataScopePolicyMapper userRolePermissionDataScopePolicyMapper,
			ObjectMapper objectMapper, UserMapper userMapper) {
		this.userRolePermissionDataScopePolicyMapper = userRolePermissionDataScopePolicyMapper;
		this.objectMapper = objectMapper;
		this.userMapper = userMapper;
	}

	/**
	 * 根据用户ID构建授权画像
	 * @param userId 用户ID
	 * @return 授权画像
	 */
	public AuthProfile buildByUserId(Long userId) {
		UserEntity user = userMapper.selectById(userId);
		if (user == null) {
			throw new AuthBusinessException(AuthResultCode.USER_NOT_FOUND);
		}
		return buildByUser(user);
	}

	/**
	 * 根据用户信息构建授权画像（避免重复查询用户）
	 * @param user 用户信息
	 * @return 授权画像
	 */
	public AuthProfile buildByUser(UserEntity user) {
		if (user == null || user.getId() == null) {
			throw new AuthBusinessException(AuthResultCode.USER_NOT_FOUND);
		}
		long userId = user.getId();

		List<String> roles = userRolePermissionDataScopePolicyMapper.selectRolesByUserId(userId);
		List<String> permissions = userRolePermissionDataScopePolicyMapper.selectPermissionsByUserId(userId);

		if (PermissionConstant.isAdmin(userId, roles)) {
			roles = PermissionConstant.ADMIN_ROLES;
			permissions = PermissionConstant.ADMIN_WILDCARD_PERMISSIONS;
		}

		List<DataScopeProjection> dataScopeItems = userRolePermissionDataScopePolicyMapper
			.selectEffectiveDataScopesByUserId(userId);
		Map<String, ScopeGrant> dataScopes = buildDataScopes(dataScopeItems);

		List<PolicyProjection> policyItems = userRolePermissionDataScopePolicyMapper
			.selectEffectivePoliciesByUserId(userId);
		PolicyBundle policyBundle = buildPolicyBundle(policyItems);

		return AuthProfile.builder()
			.userId(user.getId())
			.username(user.getUsername())
			.roles(roles)
			.permissions(permissions)
			.dataScopes(dataScopes)
			.policies(policyBundle)
			.permVersion(user.getPermVersion())
			.build();
	}

	private Map<String, ScopeGrant> buildDataScopes(List<DataScopeProjection> items) {
		if (items == null || items.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, List<DataScopeProjection>> groupedKey = new LinkedHashMap<>();
		for (DataScopeProjection row : items) {
			String rawType = row.getSubjectType();
			if (CharSequenceUtil.isBlank(rawType)) {
				continue;
			}
			groupedKey.computeIfAbsent(rawType.trim(), ignored -> new ArrayList<>()).add(row);
		}
		Map<String, ScopeGrant> result = new LinkedHashMap<>();
		for (Map.Entry<String, List<DataScopeProjection>> entry : groupedKey.entrySet()) {
			DataScopeGrantTableSubjectType subjectKey = parseSubjectType(entry.getKey());
			if (subjectKey == null) {
				continue;
			}
			ScopeGrant merged = mergeProjectionBucket(entry.getValue());
			if (merged != null) {
				result.put(subjectKey.name(), merged);
			}
		}
		return result;
	}

	private DataScopeGrantTableSubjectType parseSubjectType(String raw) {
		try {
			return DataScopeGrantTableSubjectType.valueOf(raw);
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private ScopeGrant mergeProjectionBucket(List<DataScopeProjection> rows) {
		List<ScopeGrant> grants = new ArrayList<>(rows.size());
		for (DataScopeProjection row : rows) {
			grants.add(ScopeGrant.builder()
				.scopeType(toScopeGrantType(row.getScopeType()))
				.values(parseStringList(row.getScopeSubjectIds()))
				.build());
		}
		return ScopeGrantEffectiveMerger.mergeLoose(grants);
	}

	private PolicyBundle buildPolicyBundle(List<PolicyProjection> items) {
		if (items == null || items.isEmpty()) {
			return PolicyBundle.empty();
		}

		List<PolicyRule> allow = new ArrayList<>();
		List<PolicyRule> deny = new ArrayList<>();

		for (PolicyProjection item : items) {
			Map<String, Object> params = parseMap(item.getConfig());
			params.put("policyCode", item.getPolicyCode());
			params.put("conditionType", item.getConditionType());
			params.put("conditionExpression", item.getFinalConditionExpression());
			PolicyRule policyRule = PolicyRule.builder().type(item.getPolicyType()).params(params).build();
			if ("DENY".equals(item.getFinalAction())) {
				deny.add(policyRule);
			}
			else {
				allow.add(policyRule);
			}
		}
		return PolicyBundle.builder().allow(allow).deny(deny).build();
	}

	private ScopeGrantType toScopeGrantType(String scopeType) {
		if (CharSequenceUtil.isBlank(scopeType)) {
			return ScopeGrantType.ALL;
		}
		return switch (scopeType.trim()) {
			case "ALL" -> ScopeGrantType.ALL;
			case "SELF" -> ScopeGrantType.SELF;
			case "DEPT", "CURRENT_ONLY" -> ScopeGrantType.NODE_ONLY;
			case "DEPT_AND_CHILD", "CURRENT_AND_CHILD" -> ScopeGrantType.NODE_AND_DESC;
			case "CUSTOM_POLICY" -> ScopeGrantType.CUSTOM_POLICY;
			default -> ScopeGrantType.CUSTOM;
		};
	}

	private List<String> parseStringList(String json) {
		if (CharSequenceUtil.isBlank(json)) {
			return Collections.emptyList();
		}
		try {
			List<Object> values = objectMapper.readValue(json, LIST_OBJECT_TYPE);
			List<String> out = new ArrayList<>(values.size());
			for (Object value : values) {
				out.add(String.valueOf(value));
			}
			return List.copyOf(new LinkedHashSet<>(out));
		}
		catch (Exception ignored) {
			return Collections.emptyList();
		}
	}

	private Map<String, Object> parseMap(String json) {
		if (CharSequenceUtil.isBlank(json)) {
			return new LinkedHashMap<>();
		}
		try {
			Map<String, Object> map = objectMapper.readValue(json, MAP_OBJECT_TYPE);
			return map == null ? new LinkedHashMap<>() : new LinkedHashMap<>(map);
		}
		catch (Exception ignored) {
			return new LinkedHashMap<>();
		}
	}

}
