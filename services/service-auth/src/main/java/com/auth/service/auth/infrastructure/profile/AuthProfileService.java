package com.auth.service.auth.infrastructure.profile;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.*;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.mapper.UserRolePermissionMapper;
import com.auth.service.auth.model.projection.DataScopeProjection;
import com.auth.service.auth.model.projection.PolicyProjection;
import com.auth.service.auth.model.projection.RolePermissionProjection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

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

	private final UserRolePermissionMapper userRolePermissionMapper;

	private final ObjectMapper objectMapper;

	private final UserMapper userMapper;

	public AuthProfileService(UserRolePermissionMapper userRolePermissionMapper, ObjectMapper objectMapper,
			UserMapper userMapper) {
		this.userRolePermissionMapper = userRolePermissionMapper;
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

		// 查询角色和权限
		RolePermissionProjection rolePermissionProjection = userRolePermissionMapper
			.selectRolePermissionByUserId(userId);
		List<String> roles = Optional.ofNullable(rolePermissionProjection)
			.map(RolePermissionProjection::getRoles)
			.orElseGet(List::of);
		List<String> permissions = Optional.ofNullable(rolePermissionProjection)
			.map(RolePermissionProjection::getPermissions)
			.orElseGet(List::of);

		// 如果是ADMIN需要设置前端的管理员权限使用方式
		if (PermissionConstant.isAdmin(userId, roles)) {
			roles = PermissionConstant.ADMIN_ROLES;
			permissions = PermissionConstant.ADMIN_WILDCARD_PERMISSIONS;
		}

		// 查询数据范围
		List<DataScopeProjection> dataScopeItems = userRolePermissionMapper.selectEffectiveDataScopesByUserId(userId);
		Map<String, ScopeGrant> dataScopes = buildDataScopes(dataScopeItems);

		// 查询策略
		List<PolicyProjection> policyItems = userRolePermissionMapper.selectEffectivePoliciesByUserId(userId);
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

	/**
	 * 构建数据权限维度对象
	 * @param items 数据权限行
	 * @return 维度映射
	 */
	private Map<String, ScopeGrant> buildDataScopes(List<DataScopeProjection> items) {
		if (items == null || items.isEmpty()) {
			return Collections.emptyMap();
		}
		DataScopeProjection effective = items.get(0);
		List<String> values = parseStringList(effective.getScopeDeptIds());
		ScopeGrant scopeGrant = ScopeGrant.builder()
			.mode(ScopeMode.TREE)
			.scopeType(toScopeGrantType(effective.getScopeType()))
			.values(values)
			.expandedValues(values)
			.build();

		Map<String, ScopeGrant> dataScopes = new LinkedHashMap<>();
		dataScopes.put("dept", scopeGrant);
		return dataScopes;
	}

	/**
	 * 构建策略集合
	 * @param items 策略行
	 * @return 策略包
	 */
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

	/**
	 * scope_type 到 ScopeGrantType 的映射
	 * @param scopeType scope_type
	 * @return 枚举类型
	 */
	private ScopeGrantType toScopeGrantType(String scopeType) {
		if (CharSequenceUtil.isBlank(scopeType)) {
			return ScopeGrantType.ALL;
		}
		return switch (scopeType) {
			case "ALL" -> ScopeGrantType.ALL;
			case "SELF" -> ScopeGrantType.SELF;
			case "DEPT" -> ScopeGrantType.NODE_ONLY;
			case "DEPT_AND_CHILD" -> ScopeGrantType.NODE_AND_DESC;
			case "CUSTOM_POLICY" -> ScopeGrantType.CUSTOM_POLICY;
			default -> ScopeGrantType.CUSTOM;
		};
	}

	/**
	 * 解析 JSON 数组为字符串列表
	 * @param json JSON 数组
	 * @return 字符串列表
	 */
	private List<String> parseStringList(String json) {
		if (CharSequenceUtil.isBlank(json)) {
			return Collections.emptyList();
		}
		try {
			List<Object> values = objectMapper.readValue(json, LIST_OBJECT_TYPE);
			return values.stream().map(String::valueOf).toList();
		}
		catch (Exception ignored) {
			return Collections.emptyList();
		}
	}

	/**
	 * 解析 JSON 对象为参数映射
	 * @param json JSON 字符串
	 * @return 参数映射
	 */
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
