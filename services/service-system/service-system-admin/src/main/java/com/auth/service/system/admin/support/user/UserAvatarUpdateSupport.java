package com.auth.service.system.admin.support.user;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.FileBizType;
import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.port.OwnedFileOperations;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.support.me.OldAvatarCleanupTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户头像写入与旧图清理
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class UserAvatarUpdateSupport {

	private final SysUserMapper sysUserMapper;

	private final OwnedFileOperations ownedFileOperations;

	private final OldAvatarCleanupTrigger oldAvatarCleanupTrigger;

	/**
	 * 写入头像并触发旧图清理
	 * @param targetUser 目标用户
	 * @param avatar 新头像 URL
	 * @param fileOwnerUserId 文件归属校验用的用户 ID（自助为本人，管理员为操作者）
	 */
	public void applyAvatarUpdate(UserEntity targetUser, String avatar, Long fileOwnerUserId) {
		String oldAvatar = targetUser.getAvatar();
		if (CharSequenceUtil.isBlank(avatar) || CharSequenceUtil.equals(oldAvatar, avatar)) {
			return;
		}

		try {
			ownedFileOperations.assertOwnedFileUrl(OwnedFileAssertByUrlRequest.builder()
				.url(avatar)
				.ownerUserId(fileOwnerUserId)
				.bizType(FileBizType.AVATAR.getCode())
				.build());
		}
		catch (RuntimeException ex) {
			throw new SystemBusinessException(SystemCommonResultCode.OPERATION_FAILED, ex.getMessage());
		}

		targetUser.setAvatar(avatar);
		sysUserMapper.updateById(targetUser);

		oldAvatarCleanupTrigger.cleanupAfterCommit(targetUser.getId(), oldAvatar);
	}

}
