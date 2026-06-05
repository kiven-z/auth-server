package com.auth.service.system.admin.service.impl;

import com.auth.module.file.export.impl.ExcelExportStrategy;
import com.auth.service.system.admin.convert.SysPostExportConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.excel.export.ExportPageValidator;
import com.auth.service.system.admin.excel.post.SysPostExportRow;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostExportQuery;
import com.auth.service.system.admin.service.SysPostExportService;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 岗位 Excel 导出实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysPostExportServiceImpl implements SysPostExportService {

	private static final DateTimeFormatter FILE_NAME_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private static final String SHEET_NAME = "岗位";

	private static final int STATUS_COLUMN_INDEX = 4;

	private final SysPostMapper sysPostMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysPostExportQuery query) throws IOException {
		int fromPage = query.getFromPage();
		int toPage = query.getToPage();
		int pageSize = ExportPageValidator.normalizePageSize(query.getPageSize());
		ExportPageValidator.validatePageRange(fromPage, toPage, pageSize);

		// 构建排序语句
		String orderBySql = SysPostPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		List<SysPostPageRowPO> rows = new ArrayList<>();
		for (int pageIndex = fromPage; pageIndex <= toPage; pageIndex++) {
			Page<SysPostEntity> pageParams = new Page<>(pageIndex, pageSize);
			IPage<SysPostPageRowPO> page = sysPostMapper.selectListByPage(pageParams, query, orderBySql);
			rows.addAll(page.getRecords());
		}
		List<SysPostExportRow> exportRows = SysPostExportConverter.INSTANCE.toExportRows(rows);

		// 构建文件名
		String filename = "岗位_" + LocalDateTime.now().format(FILE_NAME_TIME) + ".xlsx";
		ExcelExportStrategy<SysPostExportRow> strategy = new ExcelExportStrategy<>(SysPostExportRow.class, SHEET_NAME)
			.registerWriteHandler(new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX));

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			return strategy.export(exportRows, outputStream, filename);
		}
	}

}
