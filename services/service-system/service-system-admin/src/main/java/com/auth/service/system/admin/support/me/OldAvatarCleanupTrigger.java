package com.auth.service.system.admin.support.me;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.api.model.enums.FileBizType;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import com.auth.module.file.api.port.OwnedFileOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 头像替换后清理旧文件：事务提交后再调用文件治理接口
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class OldAvatarCleanupTrigger {

	private final OwnedFileOperations ownedFileOperations;

	/**
	 * 在事务提交后尝试撤销旧头像；无活跃事务时立即执行
	 * @param ownerUserId 文件归属用户 ID
	 * @param oldAvatarUrl 待清理的旧头像 URL
	 */
	public void cleanupAfterCommit(Long ownerUserId, String oldAvatarUrl) {
		if (ownerUserId == null || CharSequenceUtil.isBlank(oldAvatarUrl)) {
			return;
		}
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			doCleanup(ownerUserId, oldAvatarUrl);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				doCleanup(ownerUserId, oldAvatarUrl);
			}
		});
	}

	/**
	 * 尝试撤销旧头像
	 * @param ownerUserId 文件归属用户 ID
	 * @param oldAvatarUrl 待清理的旧头像 URL
	 */
	private void doCleanup(Long ownerUserId, String oldAvatarUrl) {
		try {
			ownedFileOperations.tryDeleteOwnedByUrl(OwnedFileDeleteByUrlRequest.builder()
				.url(oldAvatarUrl)
				.ownerUserId(ownerUserId)
				.deleteSource(FileDeleteSource.SYSTEM_ACTION.getCode())
				.bizType(FileBizType.AVATAR.getCode())
				.build());
		}
		catch (RuntimeException ex) {
			log.warn("Failed to cleanup old avatar: ownerUserId={}, url={}, error={}", ownerUserId, oldAvatarUrl,
					ex.getMessage());
		}
	}

}