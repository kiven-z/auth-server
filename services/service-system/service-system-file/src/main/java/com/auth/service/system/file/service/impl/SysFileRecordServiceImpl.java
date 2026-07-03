package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.enums.StoragePlatformEnum;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.convert.FileRecordQueryConverter;
import com.auth.service.system.file.mapper.SysFileRecordMapper;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.po.SysFileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.service.SysFileRecordService;
import com.auth.service.system.file.support.FileRecordReferenceChecker;
import com.auth.service.system.file.support.FileUrlSigner;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 文件记录查询与治理服务实现。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysFileRecordServiceImpl extends ServiceImpl<SysFileRecordMapper, SysFileRecordEntity>
		implements SysFileRecordService {

	private final FileRecordReferenceChecker fileRecordReferenceChecker;

	private final FileUrlSigner fileUrlSigner;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<FileRecordPageVO> getPage(FileRecordPageQuery query) {
		Page<SysFileRecordEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<SysFileRecordPageRowPO> page = baseMapper.selectListByPage(pageParams, query);

		List<FileRecordPageVO> records = page.getRecords().stream().map(row -> {
			FileRecordPageVO pageVO = FileRecordQueryConverter.INSTANCE.toPageVo(row);

			String url = pageVO.getUrl();
			String contentType = pageVO.getContentType();
			StoragePlatformEnum platform = pageVO.getStoragePlatform();

			String accessUrl = fileUrlSigner.sign(url, contentType, platform);
			pageVO.setAccessUrl(accessUrl);
			return pageVO;
		}).toList();

		Page<FileRecordPageVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
		voPage.setRecords(records);
		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public FileRecordDetailVO getDetail(Long id, Long ownerUserId) {
		SysFileRecordEntity entity = fileRecordReferenceChecker.getExistingActive(id);
		if (ownerUserId != null) {
			fileRecordReferenceChecker.requireOwner(entity, ownerUserId);
		}

		FileRecordDetailVO detail = FileRecordQueryConverter.INSTANCE.toDetailVo(entity);
		String accessUrl = fileUrlSigner.sign(entity.getUrl(), entity.getContentType(), entity.getStoragePlatform());
		detail.setAccessUrl(accessUrl);

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteById(Long id, Long ownerUserId) {
		SysFileRecordEntity entity = fileRecordReferenceChecker.getExistingActive(id);
		if (ownerUserId != null) {
			fileRecordReferenceChecker.requireOwner(entity, ownerUserId);
		}

		removeById(id);
	}

}
