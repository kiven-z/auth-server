package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.UserPostInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.auth.service.system.common.model.SystemAuditBizModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 岗位启用状态变更后触发授权失效
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PostAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按岗位 ID 提交失效（去重、忽略空）
	 * @param postIds 岗位主键
	 * @param operation 操作标识
	 */
	public void submitByPostIds(Collection<Long> postIds, String operation) {
		List<Long> ids = postIds.stream().distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		String sourceBizId = CharSequenceUtil.blankToDefault(operation, "invalidate") + ":"
				+ ids.stream().map(String::valueOf).collect(Collectors.joining(","));
		String eventId = AuthorizationChangeKind.USER_POST.eventIdPrefix() + ":" + IdWorker.getId();
		UserPostInvalidatePayload payload = new UserPostInvalidatePayload(ids);
		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest(eventId,
				AuthorizationChangeKind.USER_POST, payload);
		invalidationCoordinator.submit(request, SystemAuditBizModule.SYS_POST, sourceBizId);
	}

}
