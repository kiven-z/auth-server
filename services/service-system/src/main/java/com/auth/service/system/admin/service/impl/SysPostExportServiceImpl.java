package com.auth.service.system.admin.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.model.enums.ZebraStripeStyleType;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.service.system.admin.convert.SysPostExportConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.excel.post.SysPostExportRow;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostExportQuery;
import com.auth.service.system.admin.service.SysPostExportService;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 岗位 Excel 导出实现
 *
 * @author Bunny
 */
@Service
public class SysPostExportServiceImpl
		extends AbstractSyncPagedExportService<SysPostExportQuery, SysPostPageRowPO, SysPostExportRow>
		implements SysPostExportService {

	private static final String SHEET_NAME = "岗位";

	private static final String FILENAME_PREFIX = "岗位";

	private static final int STATUS_COLUMN_INDEX = 4;

	private final SysPostMapper sysPostMapper;

	public SysPostExportServiceImpl(SysPostMapper sysPostMapper) {
		super(buildExcelWriter(), FILENAME_PREFIX);
		this.sysPostMapper = sysPostMapper;
	}

	/**
	 * 构建 Excel 导出 writer
	 * @return Excel 导出 writer
	 */
	private static ExcelExportWriter<SysPostExportRow> buildExcelWriter() {
		return new ExcelExportWriter<>(SysPostExportRow.class, SHEET_NAME)
			.registerWriteHandler(ZebraStripeStyleType.getHandler(ZebraStripeStyleType.BLUE_GREY_TECH))
			.registerWriteHandler(new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysPostExportQuery query) throws IOException {
		return super.export(query);
	}

	@Override
	protected List<SysPostPageRowPO> fetchPage(SysPostExportQuery query, int pageIndex, int pageSize) {
		String orderBySql = SysPostPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		Page<SysPostEntity> pageParams = new Page<>(pageIndex, pageSize);
		IPage<SysPostPageRowPO> page = sysPostMapper.selectListByPage(pageParams, query, orderBySql);
		return page.getRecords();
	}

	@Override
	protected List<SysPostExportRow> toExportRows(List<SysPostPageRowPO> rows) {
		return SysPostExportConverter.INSTANCE.toExportRows(rows);
	}

}
