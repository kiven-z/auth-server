package com.auth.service.example.feign;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.core.token.model.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用鉴权服务内部接口（用于验证 Feign 出站 X-Internal-JWT）
 *
 * @author Bunny
 */
@FeignClient(name = "service-auth", path = "/api/auth/")
public interface ExampleAuthInnerFeignClient {

	/**
	 * 刷新用户缓存（与 service-system 内部客户端路径一致）
	 * @param request 刷新令牌
	 * @return 结果
	 */
	@Operation(summary = "刷新令牌")
	@PostMapping("refresh-token")
	Result<Object> refreshToken(@Valid @RequestBody RefreshTokenRequest request);

}
