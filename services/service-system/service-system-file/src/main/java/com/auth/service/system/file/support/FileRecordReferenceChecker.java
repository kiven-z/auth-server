package com.auth.service.system.file.support;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.file.exception.FileException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
	 * 加载可用文件记录，不存在或已删除则抛业务异常
	 * @param fileId 文件记录 ID
	 * @return 文件记录
	 */
	public SysFileRecordEntity getExistingActive(Long fileId) {
		SysFileRecordEntity entity = sysFileRecordMapper.selectById(fileId);
		if (entity == null) {
			throw new FileException(FileUploadResultCode.FILE_RECORD_NOT_FOUND, fileId);
		}
		return entity;
	}

	/**
	 * 校验当前用户是否为文件创建人
	 * @param entity 文件记录
	 * @param ownerUserId 归属用户ID
	 */
	public void requireOwner(SysFileRecordEntity entity, Long ownerUserId) {
		if (ownerUserId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.JWT_INVALID);
		}

		Long ownerId = entity.getCreateUser();
		if (ownerUserId.equals(ownerId)) {
			return;
		}

		throw new FileException(FileUploadResultCode.FILE_ACCESS_DENIED, entity.getId());
	}

}
