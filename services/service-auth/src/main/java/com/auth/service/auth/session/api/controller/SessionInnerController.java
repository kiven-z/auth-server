package com.auth.service.auth.session.api.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.auth.session.application.SessionAdminApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会话域内部接口：服务间批量踢下线
 *
 * @author Bunny
 */
@Tag(name = "内部会话", description = "服务间会话撤销")
@RequiredArgsConstructor
@RequestMapping("/api/auth/inner/sessions")
@RestController
public class SessionInnerController {

	private final SessionAdminApplicationService sessionAdminApplicationService;

	/**
	 * 批量踢出用户全部会话（账户禁用、删除等场景）
	 * @param userIds 用户 ID 列表
	 * @return 统一响应
	 */
	@InternalApi
	@Operation(summary = "批量踢出用户全部会话")
	@PostMapping("/kick-all")
	public Result<Void> kickAllSessions(@RequestBody(required = false) List<Long> userIds) {
		sessionAdminApplicationService.kickAllSessions(userIds);
		return Result.success();
	}

}
