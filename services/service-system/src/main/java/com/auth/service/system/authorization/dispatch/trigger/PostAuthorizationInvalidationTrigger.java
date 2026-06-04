package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.UserPostInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 岗位主表变更后触发授权失效（成员经 user_post 绑定，展开依赖 sys_post 启用状态）。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PostAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按岗位 ID 提交失效（去重、忽略空）。
	 * @param postIds 岗位主键
	 * @param operation 操作标识
	 */
	public void submitByPostIds(Collection<Long> postIds, String operation) {
		List<Long> ids = postIds.stream().distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		String bizKey = "post:" + operation + ":" + ids;
		String eventId = "sys-post-" + operation + "-" + UUID.randomUUID().toString().replace("-", "");
		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest(eventId,
				AuthorizationChangeKind.USER_POST, new UserPostInvalidatePayload(ids));
		invalidationCoordinator.submit(request, SystemAuditBizModule.SYS_POST, bizKey);
	}

}
