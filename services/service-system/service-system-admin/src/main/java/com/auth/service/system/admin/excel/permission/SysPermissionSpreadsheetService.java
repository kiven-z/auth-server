package com.auth.service.system.admin.excel.permission;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.delivery.FileDelivery;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.service.system.admin.convert.spreadsheet.SysPermissionSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.auth.service.system.admin.support.sqlbuild.SysPermissionPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 权限 Excel 导入导出
 *
 * @author Bunny
 */
@Service
public class SysPermissionSpreadsheetService
		extends AbstractSyncPagedExportService<SysPermissionQuery, SysPermissionEntity, SysPermissionSpreadsheetRow> {

	private static final String SHEET_NAME = "权限";

	private static final String FILENAME_PREFIX = "权限";

	private static final int STATUS_COLUMN_INDEX = 2;

	private final SysPermissionMapper sysPermissionMapper;

	private final PermissionSheetImporter permissionSheetImporter;

	public SysPermissionSpreadsheetService(SysPermissionMapper sysPermissionMapper,
			PermissionSheetImporter permissionSheetImporter) {
		super(ExcelExportWriter.withStatusStyle(SysPermissionSpreadsheetRow.class, SHEET_NAME,
				new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX)), FILENAME_PREFIX);
		this.sysPermissionMapper = sysPermissionMapper;
		this.permissionSheetImporter = permissionSheetImporter;
	}

	/**
	 * 按筛选条件与页码区间导出权限 Excel
	 * @param filter 列表筛选
	 * @param scope 导出范围
	 * @return 文件字节响应
	 * @throws IOException 写入失败
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysPermissionQuery filter, ExportScopeQueryFields scope) throws IOException {
		return super.export(filter, scope);
	}

	@Override
	protected List<SysPermissionEntity> fetchPage(SysPermissionQuery filter, int pageIndex, int pageSize) {
		String orderBySql = SysPermissionPageOrderSqlBuilder.buildOrderBySql(filter.getSort());
		Page<SysPermissionEntity> pageParams = new Page<>(pageIndex, pageSize);
		IPage<SysPermissionEntity> page = sysPermissionMapper.selectListByPage(pageParams, filter, orderBySql);
		return page.getRecords();
	}

	@Override
	protected List<SysPermissionSpreadsheetRow> toExportRows(List<SysPermissionEntity> rows) {
		return SysPermissionSpreadsheetConverter.INSTANCE.toSpreadsheetRows(rows);
	}

	/**
	 * 从上传文件批量导入权限（解析 → 校验 → 写入，全部通过才落库）
	 * @param file 上传的 Excel 文件
	 * @return 导入结果（含行级错误）
	 * @throws IOException 文件读取失败
	 */
	@Transactional(rollbackFor = Exception.class)
	public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
		return permissionSheetImporter.doImport(file);
	}

	/**
	 * 生成权限导入模板并返回文件响应
	 * @return 模板文件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
		SysPermissionImportRow row = new SysPermissionImportRow();
		row.setPermissionCode("（示例：sys:dept:query，全小写，1～4段冒号分隔）");
		row.setPermissionName("（示例：部门查询）");
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("（可选）");
		byte[] bytes = new ExcelExportWriter<>(SysPermissionImportRow.class, "权限导入模板").write(List.of(row));
		return FileDelivery.deliver(bytes, "permission_import_template.xlsx");
	}

}
