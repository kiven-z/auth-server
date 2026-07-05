package com.auth.service.system.file.support;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

/**
 * 文件记录存在性校验
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class FileRecordReferenceChecker {

	private final SysFileRecordMapper sysFileRecordMapper;

	/**
	 * 批量加载指定可见性下的文件记录，不满足条件则抛业务异常
	 * @param fileIds 文件记录 ID 集合
	 * @param expectedDeleted 期望的逻辑删除状态
	 * @return 文件记录
	 */
	public List<SysFileRecordEntity> getExistingBatch(List<Long> fileIds, Boolean expectedDeleted) {
		List<Long> normalizedIds = fileIds == null ? List.of()
				: fileIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(normalizedIds)) {
			return List.of();
		}

		List<SysFileRecordEntity> entities = sysFileRecordMapper.selectIsDeletedByIds(normalizedIds, expectedDeleted);
		if (CollUtil.isEmpty(entities) || entities.size() != normalizedIds.size()) {
			throw new FileException(FileUploadResultCode.FILE_RECORD_NOT_FOUND, normalizedIds);
		}
		return entities;
	}

	/**
	 * 批量校验当前用户是否为文件创建人
	 * @param entities 文件记录
	 * @param ownerUserId 归属用户ID
	 */
	public void requireOwnerBatch(List<SysFileRecordEntity> entities, Long ownerUserId) {
		if (ownerUserId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.JWT_INVALID);
		}
		if (CollUtil.isEmpty(entities)) {
			return;
		}

		boolean hasOwnerMismatch = entities.stream()
			.anyMatch(entity -> entity == null || !ownerUserId.equals(entity.getCreateUser()));
		if (hasOwnerMismatch) {
			throw new FileException(FileUploadResultCode.FILE_ACCESS_DENIED);
		}
	}

}
