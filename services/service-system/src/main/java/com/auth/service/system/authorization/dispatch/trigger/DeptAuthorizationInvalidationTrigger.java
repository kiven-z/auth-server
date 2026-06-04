package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.UserDeptInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 部门父级或启用状态变更、删除后触发授权失效
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class DeptAuthorizationInvalidationTrigger {

	private final AuthorizationInvalidationCoordinator invalidationCoordinator;

	/**
	 * 按部门 ID 提交失效（去重、忽略空）
	 * @param deptIds 部门 ID，通常含被移动/删除节点及其关联子树锚点
	 * @param operation 操作标识
	 */
	public void submitByDeptIds(Collection<Long> deptIds, String operation) {
		List<Long> ids = deptIds.stream().distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}
		String bizKey = "dept:" + operation + ":" + ids;
		String eventId = "sys-dept-" + operation + "-" + UUID.randomUUID().toString().replace("-", "");
		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest(eventId,
				AuthorizationChangeKind.USER_DEPT, new UserDeptInvalidatePayload(ids));
		invalidationCoordinator.submit(request, SystemAuditBizModule.SYS_DEPT, bizKey);
	}

}
