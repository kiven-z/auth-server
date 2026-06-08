package com.auth.service.system.admin.support.user;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.common.service.UserSessionRevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * 事务提交后撤销用户会话，避免 DB 回滚与会话已踢不一致
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserSessionRevocationSupport {

	private final UserSessionRevocationService userSessionRevocationService;

	/**
	 * 在事务提交后批量踢出用户全部会话；无活跃事务时立即执行
	 * @param userIds 用户 ID 列表
	 */
	public void revokeAllSessionsAfterCommit(List<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return;
		}

		List<Long> ids = List.copyOf(userIds);
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			userSessionRevocationService.revokeAllSessions(ids);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				userSessionRevocationService.revokeAllSessions(ids);
			}

		});
	}

}
