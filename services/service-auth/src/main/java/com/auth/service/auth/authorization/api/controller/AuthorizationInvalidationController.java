package com.auth.service.auth.authorization.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.auth.authorization.api.dto.invalidation.InvalidationResponse;
import com.auth.service.auth.authorization.api.dto.invalidation.RoleInvalidateRequest;
import com.auth.service.auth.authorization.domain.invalidation.model.ImpactTarget;
import com.auth.service.auth.authorization.domain.invalidation.model.InvalidationCommand;
import com.auth.service.auth.authorization.domain.invalidation.model.RoleCodes;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 授权失效内部接口（按业务维度分路径，编排层统一）。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@RequestMapping("/api/auth/inner/authorization")
@RestController
public class AuthorizationInvalidationController {

	@InternalApi
	@Operation(summary = "按角色码失效授权（反查用户 → 递增 perm_version → 刷新/驱逐画像）")
	@PostMapping("/invalidate/role")
	public Result<InvalidationResponse> invalidateByRole(@Valid @RequestBody RoleInvalidateRequest request) {
		ImpactTarget impactTarget = new RoleCodes(request.getRoleCodes());
		String eventId = request.getEventId();

		// TODO 待实现
		InvalidationCommand command = new InvalidationCommand(impactTarget, eventId);

		return Result.success();
	}

}
