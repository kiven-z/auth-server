package com.auth.service.auth.session.job;

import com.auth.service.auth.session.application.SessionAdminApplicationService;
import com.auth.service.auth.session.config.OnlineSessionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 全局在线会话僵尸索引 reconcile：定时清理 global ZSet 中 Hash 已不存在的 jti
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class OnlineSessionReconcileJob {

	private final SessionAdminApplicationService sessionAdminApplicationService;

	private final OnlineSessionProperties onlineSessionProperties;

	/**
	 * 定时扫描并清理 global ZSet 僵尸成员
	 */
	@Scheduled(fixedDelayString = "${auth.auth-service.online-session.reconcile.interval-ms:600000}")
	public void reconcileStaleGlobalSessions() {
		if (!Boolean.TRUE.equals(onlineSessionProperties.getReconcile().getEnabled())) {
			return;
		}

		int cleaned = sessionAdminApplicationService.reconcileStaleGlobalSessions();
		if (cleaned > 0) {
			log.info("Reconciled stale global online session indexes: count={}", cleaned);
		}
	}

}
