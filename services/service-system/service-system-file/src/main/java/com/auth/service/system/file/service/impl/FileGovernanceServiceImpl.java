package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.service.FileGovernanceService;
import com.auth.service.system.file.support.FileRecordDeletionSupport;
import com.auth.service.system.file.utils.FileObjectKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 文件 URL 归属校验与按 URL 治理实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class FileGovernanceServiceImpl implements FileGovernanceService {

	private final FileRecordMapper fileRecordMapper;

	private final FileRecordDeletionSupport fileRecordDeletionSupport;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void tryDeleteOwnedByUrl(OwnedFileDeleteByUrlRequest request) {
		String url = request.getUrl();
		Long ownerUserId = request.getOwnerUserId();
		String bizType = request.getBizType();
		String deleteSource = request.getDeleteSource();

		Optional<String> objectKey = FileObjectKeyUtil.resolveObjectKeyFromUrl(url);
		if (objectKey.isEmpty()) {
			return;
		}

		FileRecordEntity entity = fileRecordMapper.selectActiveByUrlAndOwner(null, objectKey.get(), ownerUserId,
				bizType);
		if (entity == null) {
			log.debug("Skip owned file delete by url: not found, url={}, ownerUserId={}", url, ownerUserId);
			return;
		}

		fileRecordDeletionSupport.applyDeletion(List.of(entity), deleteSource, ownerUserId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public void assertOwnedFileUrl(OwnedFileAssertByUrlRequest request) {
		String url = request.getUrl();
		Long ownerUserId = request.getOwnerUserId();
		String bizType = request.getBizType();

		FileRecordEntity entity = fileRecordMapper.selectActiveByUrlAndOwner(url, null, ownerUserId, bizType);
		if (entity == null) {
			log.warn("Owned file assert failed: url={}, ownerUserId={}, bizType={}", url, ownerUserId, bizType);
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		}
	}

}
