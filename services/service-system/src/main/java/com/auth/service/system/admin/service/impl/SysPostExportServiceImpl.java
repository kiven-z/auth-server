package com.auth.service.system.admin.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.module.file.export.impl.ExcelExportStrategy;
import com.auth.service.system.admin.convert.SysPostExportConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.excel.export.ExportLimits;
import com.auth.service.system.admin.excel.post.SysPostExportRow;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostExportQuery;
import com.auth.service.system.admin.service.SysPostExportService;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.RequiredArgsConstructor;

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
	 * 标准化每页条数
	 * @param pageSize 每页条数
	 * @return 标准化后的每页条数
	 */
	private static int normalizePageSize(Integer pageSize) {
		// FIXME 放在 admin/excel/export/ExportPageValidator.java
		int resolved = pageSize == null ? 30 : pageSize;
		if (resolved < ExportLimits.MIN_PAGE_SIZE || resolved > ExportLimits.MAX_PAGE_SIZE) {
			throw new SystemBusinessException(SystemAdminResultCode.POST_EXPORT_PAGE_SIZE_INVALID, resolved);
		}
		return resolved;
	}

	/**
	 * 验证页码范围
	 * @param fromPage 起始页码
	 * @param toPage 结束页码
	 * @param pageSize 每页条数
	 */
	private static void validatePageRange(int fromPage, int toPage, int pageSize) {
		// FIXME 放在 admin/excel/export/ExportPageValidator.java
		if (toPage < fromPage) {
			throw new SystemBusinessException(SystemAdminResultCode.POST_EXPORT_PAGE_RANGE_INVALID, fromPage, toPage);
		}

		// 检查跨页行数是否超过上限
		long spanRows = (long) (toPage - fromPage + 1) * pageSize;
		if (spanRows > ExportLimits.MAX_EXPORT_ROWS) {
			throw new SystemBusinessException(SystemAdminResultCode.POST_EXPORT_ROW_LIMIT_EXCEEDED,
					ExportLimits.MAX_EXPORT_ROWS, spanRows);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysPostExportQuery query) throws IOException {
		int fromPage = query.getFromPage();
		int toPage = query.getToPage();
		int pageSize = normalizePageSize(query.getPageSize());

		// 验证页码范围
		validatePageRange(fromPage, toPage, pageSize);

		// 构建排序语句
		String orderBySql = SysPostPageOrderSqlBuilder.buildOrderBySql(query.getSort());

		// 加载行数据
		List<SysPostPageRowPO> rows = loadRows(query, fromPage, toPage, pageSize, orderBySql);
		List<SysPostExportRow> exportRows = SysPostExportConverter.INSTANCE.toExportRows(rows);

		// 构建文件名
		String filename = "岗位_" + LocalDateTime.now().format(FILE_NAME_TIME) + ".xlsx";
		ExcelExportStrategy<SysPostExportRow> strategy = new ExcelExportStrategy<>(SysPostExportRow.class, SHEET_NAME)
			.registerWriteHandler(new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX));

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			return strategy.export(exportRows, outputStream, filename);
		}
	}

	/**
	 * 加载行数据
	 * @param query 查询条件
	 * @param fromPage 起始页码
	 * @param toPage 结束页码
	 * @param pageSize 每页条数
	 * @param orderBySql 排序语句
	 * @return 行数据列表
	 */
	private List<SysPostPageRowPO> loadRows(SysPostExportQuery query, int fromPage, int toPage, int pageSize,
			String orderBySql) {
		// FIXME 放在support/post/PostExportPageValidator.java
		List<SysPostPageRowPO> rows = new ArrayList<>();
		for (int pageIndex = fromPage; pageIndex <= toPage; pageIndex++) {
			Page<SysPostEntity> pageParams = new Page<>(pageIndex, pageSize);
			IPage<SysPostPageRowPO> page = sysPostMapper.selectListByPage(pageParams, query, orderBySql);
			rows.addAll(page.getRecords());
		}
		return rows;
	}

}
