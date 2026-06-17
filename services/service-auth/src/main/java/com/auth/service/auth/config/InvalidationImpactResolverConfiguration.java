package com.auth.service.auth.config;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.*;
import com.auth.service.auth.application.authorization.invalidation.impact.ImpactResolver;
import com.auth.service.auth.application.authorization.invalidation.impact.PortBackedImpactResolver;
import com.auth.service.auth.application.port.invalidation.AuthorizationImpactQueryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * 影响面解析器装配：按 {@link AuthorizationChangeKind} 注册 {@link ImpactResolver} Bean。
 *
 * @author Bunny
 */
@Configuration
public class InvalidationImpactResolverConfiguration {

	/**
	 * 按角色码反查受影响用户 ID。
	 * @param port 授权影响面查询端口
	 * @return 角色码影响面解析器
	 */
	@Bean
	ImpactResolver<RoleInvalidatePayload> roleImpactResolver(AuthorizationImpactQueryPort port) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.ROLE, RoleInvalidatePayload.class,
				payload -> port.findUserIdsByRoleCodes(payload.roleCodes()));
	}

	/**
	 * 按权限码反查受影响用户 ID（P4→P1 桥接委托 Port）。
	 * @param port 授权影响面查询端口
	 * @return 权限码影响面解析器
	 */
	@Bean
	ImpactResolver<PermissionInvalidatePayload> permissionImpactResolver(AuthorizationImpactQueryPort port) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.PERMISSION, PermissionInvalidatePayload.class,
				payload -> port.findUserIdsByPermissionCodes(payload.permissionCodes()));
	}

	/**
	 * 按策略码反查受影响用户 ID（用户直连绑定 + 可选角色桥接）。
	 * @param port 授权影响面查询端口
	 * @return 策略码影响面解析器
	 */
	@Bean
	ImpactResolver<PolicyInvalidatePayload> policyImpactResolver(AuthorizationImpactQueryPort port) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.POLICY, PolicyInvalidatePayload.class,
				payload -> port.findUserIdsByPolicyCodes(payload.policyCodes()));
	}

	/**
	 * 按 grant_table 授权主体反查受影响用户 ID（P2）。
	 * @param port 授权影响面查询端口
	 * @return 授权主体影响面解析器
	 */
	@Bean
	ImpactResolver<GrantInvalidatePayload> grantSubjectImpactResolver(AuthorizationImpactQueryPort port) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.GRANT, GrantInvalidatePayload.class,
				payload -> port.findUserIdsByGrantSubjects(payload.subjects()));
	}

	/**
	 * 按部门 ID 反查成员用户 ID（P3，含子部门）。
	 * @param port 授权影响面查询端口
	 * @return 部门成员影响面解析器
	 */
	@Bean
	ImpactResolver<UserDeptInvalidatePayload> userDeptImpactResolver(AuthorizationImpactQueryPort port) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.USER_DEPT, UserDeptInvalidatePayload.class,
				payload -> port.findUserIdsByDeptIds(payload.deptIds()));
	}

	/**
	 * 按岗位 ID 反查成员用户 ID（P3）。
	 * @param port 授权影响面查询端口
	 * @return 岗位成员影响面解析器
	 */
	@Bean
	ImpactResolver<UserPostInvalidatePayload> userPostImpactResolver(AuthorizationImpactQueryPort port) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.USER_POST, UserPostInvalidatePayload.class,
				payload -> port.findUserIdsByPostIds(payload.postIds()));
	}

	/**
	 * 按用户 ID 直连失效（用户主档变更，无需 SQL 反查）。
	 * @return 用户直连影响面解析器
	 */
	@Bean
	ImpactResolver<UserInvalidatePayload> userImpactResolver() {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.USER, UserInvalidatePayload.class,
				payload -> Set.copyOf(payload.userIds()));
	}

}
