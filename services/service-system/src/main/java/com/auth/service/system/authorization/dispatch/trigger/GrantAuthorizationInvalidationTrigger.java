package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.GrantInvalidatePayload;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * grant_table 授权主体变更后触发用户授权画像失效
 *
 * @author bunny
 */
@RequiredArgsConstructor
@Component
public class GrantAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按授权主体提交失效事件
	 * @param subjects 授权主体键列表
	 * @param operation 操作标识
	 */
	public void submitByGrantSubjects(Collection<GrantSubjectKey> subjects, String operation) {
		List<GrantSubjectKey> keys = subjects.stream().distinct().toList();
		if (CollUtil.isEmpty(keys)) {
			return;
		}

		String bizKey = CharSequenceUtil.blankToDefault(operation, "invalidate") + ":grant-subjects";
		String eventId = "sys-grant-" + CharSequenceUtil.blankToDefault(operation, "op") + "-"
				+ UUID.randomUUID().toString().replace("-", "");

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest(eventId,
				AuthorizationChangeKind.GRANT, new GrantInvalidatePayload(keys));
		invalidationCoordinator.submit(request, SystemAuditBizModule.GRANT_TABLE, bizKey);
	}

}
