package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 角色维度授权失效触发：写 Outbox 并在提交后同步投递 auth。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class RoleAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按角色码提交失效事件（去重、忽略空白）。
	 * @param roleCodes 角色编码，可含 old+new
	 * @param operation 操作标识，用于 eventId 与 sourceBizId
	 */
	public void submitByRoleCodes(Collection<String> roleCodes, String operation) {
		List<String> codes = roleCodes.stream().distinct().toList();
		if (CollUtil.isEmpty(codes)) {
			return;
		}

		String bizKey = CharSequenceUtil.blankToDefault(operation, "invalidate") + ":" + String.join(",", codes);

		String eventId = "sys-role-" + CharSequenceUtil.blankToDefault(operation, "op") + "-"
				+ UUID.randomUUID().toString().replace("-", "");

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest(eventId,
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(codes));
		invalidationCoordinator.submit(request, SystemAuditBizModule.SYS_ROLE, bizKey);
	}

}
