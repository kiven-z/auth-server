package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.convert.FileRecordQueryConverter;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.po.FileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.FileRecordCommonService;
import com.auth.service.system.file.support.FileUrlSigner;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 文件记录公共服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class FileRecordCommonServiceImpl extends ServiceImpl<FileRecordMapper, FileRecordEntity>
		implements FileRecordCommonService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final FileUrlSigner fileUrlSigner;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<FileRecordPageVO> getPage(FileRecordPageQuery query, Boolean isDeleted) {
		Page<FileRecordEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());

		IPage<FileRecordPageRowPO> page = baseMapper.selectListByPage(pageParams, query, isDeleted);
		IPage<FileRecordPageVO> voPage = page.convert(FileRecordQueryConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileRecordDetailVO getDetail(Long id, Long ownerUserId, Boolean isPrivate, boolean expectedDeleted,
			String deleteSource) {
		FileRecordEntity entity = baseMapper.selectDetailById(id, ownerUserId, isPrivate, expectedDeleted,
				deleteSource);
		if (entity == null) {
			log.warn("File record not found or unavailable: id={}, ownerUserId={}", id, ownerUserId);
			throw new FileStorageException(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
		}

		FileRecordDetailVO detailVo = FileRecordQueryConverter.INSTANCE.toDetailVo(entity);
		String bucket = entity.getBucket();
		String objectKey = entity.getObjectKey();
		String contentType = entity.getContentType();
		StoragePlatformEnum storagePlatform = entity.getStoragePlatform();
		String accessUrl = fileUrlSigner.sign(bucket, objectKey, contentType, storagePlatform);

		detailVo.setAccessUrl(accessUrl);
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detailVo), null, null);

		return detailVo;
	}

}
