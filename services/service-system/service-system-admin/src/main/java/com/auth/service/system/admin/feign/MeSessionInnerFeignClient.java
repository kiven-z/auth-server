package com.auth.service.system.admin.feign;

import java.util.List;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.feign.dto.UserSessionRemoteDTO;
import com.auth.service.system.admin.feign.fallback.MeSessionInnerFeignClientFallback;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 个人中心会话内部 Feign 客户端
 *
 * @author Bunny
 */
@FeignClient(name = "service-auth", contextId = "meSessionInnerFeignClient", path = "/api/auth/inner/sessions",
		fallback = MeSessionInnerFeignClientFallback.class)
public interface MeSessionInnerFeignClient {

	/**
	 * 查询用户活跃会话列表
	 * @param userId 用户 ID
	 * @return 活跃会话列表
	 */
	@GetMapping("/users/{userId}/sessions")
	Result<List<UserSessionRemoteDTO>> listUserSessions(@PathVariable("userId") Long userId);

	/**
	 * 踢出用户指定会话
	 * @param userId 用户 ID
	 * @param sessionId 会话 ID（jti）
	 * @return 统一响应
	 */
	@PostMapping("/users/{userId}/sessions/{sessionId}/kick")
	Result<Void> kickUserSession(@PathVariable("userId") Long userId, @PathVariable("sessionId") String sessionId);

}
