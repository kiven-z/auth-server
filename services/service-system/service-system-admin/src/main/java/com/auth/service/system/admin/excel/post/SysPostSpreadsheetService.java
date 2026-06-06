package com.auth.service.system.admin.excel.post;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.model.enums.ZebraStripeStyleType;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.service.system.admin.convert.SysPostSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * 岗位 Excel 导入导出
 *
 * @author Bunny
 */
@Service
public class SysPostSpreadsheetService
		extends AbstractSyncPagedExportService<SysPostQuery, SysPostPageRowPO, SysPostSpreadsheetRow> {

	private static final String SHEET_NAME = "岗位";

	private static final String FILENAME_PREFIX = "岗位";

	private static final int STATUS_COLUMN_INDEX = 4;

	private final SysPostMapper sysPostMapper;

	public SysPostSpreadsheetService(SysPostMapper sysPostMapper) {
		super(buildExcelWriter(), FILENAME_PREFIX);
		this.sysPostMapper = sysPostMapper;
	}

	/**
	 * 构建 Excel 导出 writer
	 * @return Excel 导出 writer
	 */
	private static ExcelExportWriter<SysPostSpreadsheetRow> buildExcelWriter() {
		return new ExcelExportWriter<>(SysPostSpreadsheetRow.class, SHEET_NAME)
			.registerWriteHandler(ZebraStripeStyleType.getHandler(ZebraStripeStyleType.BLUE_GREY_TECH))
			.registerWriteHandler(new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX));
	}

	/**
	 * 按筛选条件与页码区间导出岗位 Excel
	 * @param filter 列表筛选
	 * @param scope 导出范围
	 * @return 文件字节响应
	 * @throws IOException 写入失败
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysPostQuery filter, ExportScopeQueryFields scope) throws IOException {
		return super.export(filter, scope);
	}

	@Override
	protected List<SysPostPageRowPO> fetchPage(SysPostQuery filter, int pageIndex, int pageSize) {
		String orderBySql = SysPostPageOrderSqlBuilder.buildOrderBySql(filter.getSort());
		Page<SysPostEntity> pageParams = new Page<>(pageIndex, pageSize);
		IPage<SysPostPageRowPO> page = sysPostMapper.selectListByPage(pageParams, filter, orderBySql);
		return page.getRecords();
	}

	@Override
	protected List<SysPostSpreadsheetRow> toExportRows(List<SysPostPageRowPO> rows) {
		return SysPostSpreadsheetConverter.INSTANCE.toSpreadsheetRows(rows);
	}

}
