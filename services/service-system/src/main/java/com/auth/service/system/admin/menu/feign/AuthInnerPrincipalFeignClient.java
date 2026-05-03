package com.auth.service.system.admin.menu.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.menu.feign.dto.EffectiveCodesVO;
import com.auth.service.system.admin.menu.feign.fallback.AuthInnerPrincipalFeignClientFallback;

/**
 * 调用认证服务内部接口：解析用户生效角色码与权限码（动态菜单等）
 *
 * @author Bunny
 */
@FeignClient(name = "service-auth", contextId = "authenticationInnerPrincipal", path = "/api/auth/inner",
		fallback = AuthInnerPrincipalFeignClientFallback.class)
public interface AuthInnerPrincipalFeignClient {

	/**
	 * 查询用户生效角色码与权限码
	 * @param userId 用户 ID
	 * @return 统一响应体
	 */
	@GetMapping("/principal/{userId}/effective-codes")
	Result<EffectiveCodesVO> getEffectiveCodes(@PathVariable("userId") Long userId);

}
