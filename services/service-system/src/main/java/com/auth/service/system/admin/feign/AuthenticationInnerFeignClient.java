package com.auth.service.system.admin.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.feign.dto.EffectiveCodesVO;
import com.auth.service.system.admin.feign.dto.GrantSubjectInnerRequest;
import com.auth.service.system.admin.feign.fallback.AuthenticationInnerFeignClientFallback;

/**
 * 鉴权服务内部服务,鉴权服务内部服务接口
 *
 * @author Bunny
 */
@FeignClient(name = "service-auth", contextId = "authenticationInnerCache", path = "/api/auth/inner",
		fallback = AuthenticationInnerFeignClientFallback.class)
public interface AuthenticationInnerFeignClient {

	/**
	 * 刷新用户缓存
	 * @param userIds 用户ID列表
	 * @return 刷新结果
	 */
	@PostMapping("/cache/refresh")
	Result<String> refreshUserCache(@RequestBody List<Long> userIds);

	/**
	 * 查询用户生效角色码与权限码
	 * @param userId 用户 ID
	 * @return 统一响应体
	 */
	@GetMapping("/principal/{userId}/effective-codes")
	Result<EffectiveCodesVO> getEffectiveCodes(@PathVariable("userId") Long userId);

	/**
	 * 查询持有指定角色的所有用户 ID
	 * @param roleCode 角色编码
	 * @return 用户 ID 列表
	 */
	@GetMapping("/role/{roleCode}/users")
	Result<List<Long>> listUserIdsByRoleCode(@PathVariable("roleCode") String roleCode);

	@PostMapping("/impact/users/by-role-codes")
	Result<List<Long>> listUserIdsByRoleCodes(@RequestBody List<String> roleCodes);

	@PostMapping("/impact/users/by-permission-codes")
	Result<List<Long>> listUserIdsByPermissionCodes(@RequestBody List<String> permissionCodes);

	@PostMapping("/impact/users/by-policy-codes")
	Result<List<Long>> listUserIdsByPolicyCodes(@RequestBody List<String> policyCodes);

	@PostMapping("/impact/users/by-dept-ids")
	Result<List<Long>> listUserIdsByDeptIds(@RequestBody List<Long> deptIds);

	@PostMapping("/impact/users/by-post-ids")
	Result<List<Long>> listUserIdsByPostIds(@RequestBody List<Long> postIds);

	@PostMapping("/impact/users/by-grant-subjects")
	Result<List<Long>> listUserIdsByGrantSubjects(@RequestBody List<GrantSubjectInnerRequest> subjects);

}
