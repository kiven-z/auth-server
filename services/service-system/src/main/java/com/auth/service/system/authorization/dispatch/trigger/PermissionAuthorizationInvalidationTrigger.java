package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.PermissionInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 权限码或启用状态变更、删除后触发授权失效
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PermissionAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按权限码提交失效事件（去重、忽略空白）
	 * @param permissionCodes 权限码列表
	 * @param operation 操作标识，用于 eventId 与 sourceBizId
	 */
	public void submitByPermissionCodes(Collection<String> permissionCodes, String operation) {
		List<String> codes = permissionCodes.stream().distinct().toList();
		if (CollUtil.isEmpty(codes)) {
			return;
		}

		String bizKey = CharSequenceUtil.blankToDefault(operation, "invalidate") + ":" + String.join(",", codes);

		String eventId = "sys-perm-" + CharSequenceUtil.blankToDefault(operation, "op") + "-"
				+ UUID.randomUUID().toString().replace("-", "");

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest(eventId,
				AuthorizationChangeKind.PERMISSION, new PermissionInvalidatePayload(codes));
		invalidationCoordinator.submit(request, SystemAuditBizModule.SYS_PERMISSION, bizKey);
	}

}
