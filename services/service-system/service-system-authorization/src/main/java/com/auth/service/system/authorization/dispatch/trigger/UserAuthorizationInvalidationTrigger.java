package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.UserInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.auth.service.system.common.model.SystemAuditBizModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户相关变更后按 userId 直连触发授权失效（主档、user_scope 等）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按用户 ID 提交失效（去重、忽略空）
	 * @param userIds 用户主键
	 * @param operation 操作动词，如 update、delete
	 */
	public void submitByUserIds(Collection<Long> userIds, String operation) {
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		String sourceBizId = CharSequenceUtil.blankToDefault(operation, "invalidate") + ":"
				+ ids.stream().map(String::valueOf).collect(Collectors.joining(","));
		String eventId = AuthorizationChangeKind.USER.eventIdPrefix() + ":" + IdWorker.getId();
		UserInvalidatePayload payload = new UserInvalidatePayload(ids);
		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest(eventId,
				AuthorizationChangeKind.USER, payload);
		invalidationCoordinator.submit(request, SystemAuditBizModule.SYS_USER, sourceBizId);
	}

}
