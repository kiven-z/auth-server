package com.auth.service.system.admin.user.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.user.feign.fallback.AuthenticationInnerFeignClientFallback;

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

}
