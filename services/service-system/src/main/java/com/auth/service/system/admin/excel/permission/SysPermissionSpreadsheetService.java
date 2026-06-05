package com.auth.service.system.admin.excel.permission;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.model.enums.ZebraStripeStyleType;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.service.system.admin.convert.SysPermissionSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.query.permission.SysPermissionExportQuery;
import com.auth.service.system.admin.support.sqlbuild.SysPermissionPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * 权限 Excel 导入导出
 *
 * @author Bunny
 */
@Service
public class SysPermissionSpreadsheetService extends
		AbstractSyncPagedExportService<SysPermissionExportQuery, SysPermissionEntity, SysPermissionSpreadsheetRow> {

	private static final String SHEET_NAME = "权限";

	private static final String FILENAME_PREFIX = "权限";

	private static final int STATUS_COLUMN_INDEX = 3;

	private final SysPermissionMapper sysPermissionMapper;

	public SysPermissionSpreadsheetService(SysPermissionMapper sysPermissionMapper) {
		super(buildExcelWriter(), FILENAME_PREFIX);
		this.sysPermissionMapper = sysPermissionMapper;
	}

	/**
	 * 构建 Excel 导出 writer
	 * @return Excel 导出 writer
	 */
	private static ExcelExportWriter<SysPermissionSpreadsheetRow> buildExcelWriter() {
		return new ExcelExportWriter<>(SysPermissionSpreadsheetRow.class, SHEET_NAME)
			.registerWriteHandler(ZebraStripeStyleType.getHandler(ZebraStripeStyleType.BLUE_GREY_TECH))
			.registerWriteHandler(new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX));
	}

	/**
	 * 按筛选条件与页码区间导出权限 Excel
	 * @param query 导出查询
	 * @return 文件字节响应
	 * @throws IOException 写入失败
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysPermissionExportQuery query) throws IOException {
		return super.export(query);
	}

	@Override
	protected List<SysPermissionEntity> fetchPage(SysPermissionExportQuery query, int pageIndex, int pageSize) {
		String orderBySql = SysPermissionPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		Page<SysPermissionEntity> pageParams = new Page<>(pageIndex, pageSize);
		IPage<SysPermissionEntity> page = sysPermissionMapper.selectListByPage(pageParams, query, orderBySql);
		return page.getRecords();
	}

	@Override
	protected List<SysPermissionSpreadsheetRow> toExportRows(List<SysPermissionEntity> rows) {
		return SysPermissionSpreadsheetConverter.INSTANCE.toSpreadsheetRows(rows);
	}

}
