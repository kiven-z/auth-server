package com.auth.service.system.admin.excel.role;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.service.system.admin.convert.SysRoleSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * 角色 Excel 导入导出
 *
 * @author Bunny
 */
@Service
public class SysRoleSpreadsheetService
		extends AbstractSyncPagedExportService<SysRoleQuery, SysRoleEntity, SysRoleSpreadsheetRow> {

	private static final String SHEET_NAME = "角色";

	private static final String FILENAME_PREFIX = "角色";

	private static final int STATUS_COLUMN_INDEX = 3;

	private final SysRoleMapper sysRoleMapper;

	public SysRoleSpreadsheetService(SysRoleMapper sysRoleMapper) {
		super(ExcelExportWriter.withStatusStyle(SysRoleSpreadsheetRow.class, SHEET_NAME,
				new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX)), FILENAME_PREFIX);
		this.sysRoleMapper = sysRoleMapper;
	}

	/**
	 * 按筛选条件与页码区间导出角色 Excel
	 * @param filter 列表筛选
	 * @param scope 导出范围
	 * @return 文件字节响应
	 * @throws IOException 写入失败
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysRoleQuery filter, ExportScopeQueryFields scope) throws IOException {
		return super.export(filter, scope);
	}

	@Override
	protected List<SysRoleEntity> fetchPage(SysRoleQuery filter, int pageIndex, int pageSize) {
		String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(filter.getSort());
		Page<SysRoleEntity> pageParams = new Page<>(pageIndex, pageSize);
		IPage<SysRoleEntity> page = sysRoleMapper.selectListByPage(pageParams, filter, orderBySql);
		return page.getRecords();
	}

	@Override
	protected List<SysRoleSpreadsheetRow> toExportRows(List<SysRoleEntity> rows) {
		return SysRoleSpreadsheetConverter.INSTANCE.toSpreadsheetRows(rows);
	}

}
