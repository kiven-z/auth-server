package com.auth.service.system.common.controller;

import java.util.List;
import java.util.Map;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.service.system.common.service.AuditUserDisplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对内接口-按用户 ID 批量解析用户名
 *
 * @author Bunny
 */
@Tag(name = "系统服务-内部-审计展示名")
@RequestMapping("/api/system/inner/audit-user")
@RestController
public class InternalAuditUserDisplayController {

	private final AuditUserDisplayService auditUserDisplayService;

	public InternalAuditUserDisplayController(AuditUserDisplayService auditUserDisplayService) {
		this.auditUserDisplayService = auditUserDisplayService;
	}

	@InternalApi
	@Operation(summary = "批量解析用户名", description = "根据用户主键列表返回 id → username 映射；缺失的用户不出现在结果中")
	@PostMapping("/usernames")
	public Result<Map<Long, String>> mapUsernames(@RequestBody List<Long> userIds) {
		Map<Long, String> map = auditUserDisplayService.mapUsernamesByIds(userIds);
		return Result.success(map);
	}

}
