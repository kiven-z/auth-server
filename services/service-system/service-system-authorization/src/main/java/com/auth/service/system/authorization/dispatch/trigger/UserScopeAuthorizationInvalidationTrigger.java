package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.UserScopeInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.authorization.dispatch.support.InvalidationEventIds;
import com.auth.service.system.authorization.dispatch.support.InvalidationSourceBizIds;
import com.auth.service.system.common.model.SystemAuditBizModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 用户档案变更（部门/岗位/数据范围、状态、删除等）后按 userId 直连触发授权失效
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserScopeAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按用户 ID 提交失效（去重、忽略空）
	 * @param userIds 用户主键
	 * @param operation 操作动词，如 update、delete、disable、enable
	 */
	public void submitByUserIds(Collection<Long> userIds, String operation) {
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		String sourceBizId = InvalidationSourceBizIds.of(operation, InvalidationSourceBizIds.joinLongIds(ids));
		String eventId = InvalidationEventIds.next(InvalidationEventIds.KIND_USER);
		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest(eventId,
				AuthorizationChangeKind.USER_SCOPE, new UserScopeInvalidatePayload(ids));
		invalidationCoordinator.submit(request, SystemAuditBizModule.SYS_USER, sourceBizId);
	}

}
