package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.UserPostInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.authorization.dispatch.support.InvalidationEventIds;
import com.auth.service.system.authorization.dispatch.support.InvalidationSourceBizIds;
import com.auth.service.system.common.model.audit.SystemAuditBizModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

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
     *
     * @param postIds   岗位主键
     * @param operation 操作标识
     */
    public void submitByPostIds(Collection<Long> postIds, String operation) {
        List<Long> ids = postIds.stream().distinct().toList();
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        String sourceBizId = InvalidationSourceBizIds.of(operation, InvalidationSourceBizIds.joinLongIds(ids));
        String eventId = InvalidationEventIds.next(InvalidationEventIds.KIND_POST);
        AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest(eventId,
                AuthorizationChangeKind.USER_POST, new UserPostInvalidatePayload(ids));
        invalidationCoordinator.submit(request, SystemAuditBizModule.SYS_POST, sourceBizId);
    }

}
