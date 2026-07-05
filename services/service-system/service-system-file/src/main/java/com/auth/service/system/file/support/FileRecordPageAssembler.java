package com.auth.service.system.file.support;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.convert.FileRecordQueryConverter;
import com.auth.service.system.file.model.entity.SysFileRecordEntity;
import com.auth.service.system.file.model.po.SysFileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;

/**
 * 文件记录分页结果组装
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class FileRecordPageAssembler {

	private final FileUrlSigner fileUrlSigner;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * 执行分页查询并组装签名访问地址与审计用户名
	 * @param query 查询条件
	 * @param pageLoader 分页加载函数
	 * @return 分页结果
	 */
	public PageResponse<FileRecordPageVO> assemblePage(FileRecordPageQuery query,
			BiFunction<Page<SysFileRecordEntity>, FileRecordPageQuery, IPage<SysFileRecordPageRowPO>> pageLoader) {
		Page<SysFileRecordEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<SysFileRecordPageRowPO> page = pageLoader.apply(pageParams, query);

		List<FileRecordPageVO> records = page.getRecords().stream().map(row -> {
			FileRecordPageVO pageVO = FileRecordQueryConverter.INSTANCE.toPageVo(row);
			String accessUrl = fileUrlSigner.sign(pageVO.getBucket(), pageVO.getObjectKey(), pageVO.getContentType(),
					pageVO.getStoragePlatform());
			pageVO.setAccessUrl(accessUrl);
			return pageVO;
		}).toList();

		Page<FileRecordPageVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
		voPage.setRecords(records);
		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

}
