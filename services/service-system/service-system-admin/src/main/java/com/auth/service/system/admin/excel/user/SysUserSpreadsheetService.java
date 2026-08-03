package com.auth.service.system.admin.excel.user;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.delivery.FileDelivery;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.convert.spreadsheet.SysUserSpreadsheetConverter;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.support.sqlbuild.SysUserPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 用户 Excel 导出
 *
 * @author Bunny
 */
@Service
public class SysUserSpreadsheetService
		extends AbstractSyncPagedExportService<SysUserPageQuery, SysUserPageRowPO, SysUserSpreadsheetRow> {

	private static final String SHEET_NAME = "用户";

	private static final String FILENAME_PREFIX = "用户";

	private static final int STATUS_COLUMN_INDEX = 5;

	private final SysUserMapper sysUserMapper;

	private final UserSheetImporter userSheetImporter;

	public SysUserSpreadsheetService(SysUserMapper sysUserMapper, UserSheetImporter userSheetImporter) {
		super(ExcelExportWriter.withStatusStyle(SysUserSpreadsheetRow.class, SHEET_NAME,
				new UserStatusColorCellWriteHandler(STATUS_COLUMN_INDEX)), FILENAME_PREFIX);
		this.sysUserMapper = sysUserMapper;
		this.userSheetImporter = userSheetImporter;
	}

	/**
	 * 按筛选条件与页码区间导出用户 Excel
	 * @param filter 列表筛选
	 * @param scope 导出范围
	 * @return 文件字节响应
	 * @throws IOException 写入失败
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysUserPageQuery filter, ExportScopeQueryFields scope) throws IOException {
		return super.export(filter, scope);
	}

	@Override
	protected List<SysUserPageRowPO> fetchPage(SysUserPageQuery filter, int pageIndex, int pageSize) {
		String orderBySql = SysUserPageOrderSqlBuilder.buildOrderBySql(filter.getSort());
		Page<UserEntity> pageParams = new Page<>(pageIndex, pageSize);
		IPage<SysUserPageRowPO> page = sysUserMapper.selectListByPage(pageParams, filter, orderBySql);
		return page.getRecords();
	}

	@Override
	protected List<SysUserSpreadsheetRow> toExportRows(List<SysUserPageRowPO> rows) {
		return SysUserSpreadsheetConverter.INSTANCE.toSpreadsheetRows(rows);
	}

	/**
	 * 从上传文件批量导入用户（解析 → 校验 → 写入，全部通过才落库）
	 * @param file 上传的 Excel 文件
	 * @return 导入结果（含行级错误）
	 * @throws IOException 文件读取失败
	 */
	@Transactional(rollbackFor = Exception.class)
	public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
		return userSheetImporter.doImport(file);
	}

	/**
	 * 生成用户导入模板并返回文件响应
	 * @return 模板文件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
		SysUserImportRow row = new SysUserImportRow();
		row.setUsername("（示例：zhangsan，全局唯一）");
		row.setNickname("（示例：张三）");
		row.setEmail("（示例：zhangsan@example.com，全局唯一）");
		row.setPhone("（示例：13800000000，全局唯一）");
		row.setEmployeeNo("（可选，示例：E001，全局唯一）");
		row.setStatusLabel("启用");
		row.setInitialPassword("（示例：Abcd1234，8-18位）");
		row.setGenderLabel("（可选，男/女/未知）");
		row.setBirthday("（可选，示例：1990-01-01）");
		row.setRemark("（可选）");
		byte[] bytes = new ExcelExportWriter<>(SysUserImportRow.class, "用户导入模板").write(List.of(row));
		return FileDelivery.deliver(bytes, "user_import_template.xlsx");
	}

}
