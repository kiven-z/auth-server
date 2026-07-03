package com.auth.service.system.file.support;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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
	 * 批量加载可用文件记录，不存在或已删除则抛业务异常
	 * @param fileIds 文件记录 ID 集合
	 * @return 文件记录
	 */
	public List<SysFileRecordEntity> getExistingActiveBatch(List<Long> fileIds) {
		List<Long> normalizedIds = fileIds == null ? List.of()
				: fileIds.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(normalizedIds)) {
			return List.of();
		}

		List<SysFileRecordEntity> entities = sysFileRecordMapper
			.selectList(Wrappers.<SysFileRecordEntity>lambdaQuery().in(SysFileRecordEntity::getId, normalizedIds));
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
