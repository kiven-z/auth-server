package com.auth.service.system.admin.excel.role;

import java.io.IOException;
import java.util.List;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.delivery.SyncFileDelivery;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.service.system.admin.convert.spreadsheet.SysRoleSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

	private static final int STATUS_COLUMN_INDEX = 2;

	private final SysRoleMapper sysRoleMapper;

	private final RoleSheetImporter roleSheetImporter;

	public SysRoleSpreadsheetService(SysRoleMapper sysRoleMapper, RoleSheetImporter roleSheetImporter) {
		super(ExcelExportWriter.withStatusStyle(SysRoleSpreadsheetRow.class, SHEET_NAME,
				new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX)), FILENAME_PREFIX);
		this.sysRoleMapper = sysRoleMapper;
		this.roleSheetImporter = roleSheetImporter;
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

	/**
	 * 从上传文件批量导入角色（解析 → 校验 → 写入，全部通过才落库）
	 * @param file 上传的 Excel 文件
	 * @return 导入结果（含行级错误）
	 * @throws IOException 文件读取失败
	 */
	@Transactional(rollbackFor = Exception.class)
	public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
		return roleSheetImporter.doImport(file);
	}

	/**
	 * 生成角色导入模板并返回文件响应
	 * @return 模板文件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
		SysRoleImportRow row = new SysRoleImportRow();
		row.setRoleCode("（示例：ADMIN，全大写+下划线，首字符大写字母）");
		row.setRoleName("（示例：系统管理员）");
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("（可选）");
		byte[] bytes = new ExcelExportWriter<>(SysRoleImportRow.class, "角色导入模板").write(List.of(row));
		return SyncFileDelivery.deliver(bytes, "role_import_template.xlsx");
	}

}
