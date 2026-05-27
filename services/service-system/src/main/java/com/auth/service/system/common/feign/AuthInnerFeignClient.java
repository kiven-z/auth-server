package com.auth.service.system.common.feign;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.system.common.feign.dto.EffectiveCodesInnerDTO;
import com.auth.service.system.common.feign.fallback.AuthInnerFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 鉴权服务内部 Feign 客户端
 *
 * @author Bunny
 */
@FeignClient(name = "service-auth", contextId = "authInnerFeignClient", path = "/api/auth/inner",
		fallback = AuthInnerFeignClientFallback.class)
public interface AuthInnerFeignClient {

	/**
	 * 刷新用户缓存
	 * @param userIds 用户ID列表
	 * @return 刷新结果
	 */
	@PostMapping("/authorization/cache/refresh")
	Result<String> refreshUserCache(@RequestBody List<Long> userIds);

	/**
	 * 查询用户生效角色码与权限码
	 * @param userId 用户 ID
	 * @return 统一响应体
	 */
	@GetMapping("/authorization/principal/{userId}/effective-codes")
	Result<EffectiveCodesInnerDTO> getEffectiveCodes(@PathVariable("userId") Long userId);

	/**
	 * 触发授权失效与主体画像更新
	 */
	@PostMapping("/authorization/invalidate")
	Result<AuthorizationInvalidateResponse> invalidate(@RequestBody AuthorizationInvalidateRequest request);

}
