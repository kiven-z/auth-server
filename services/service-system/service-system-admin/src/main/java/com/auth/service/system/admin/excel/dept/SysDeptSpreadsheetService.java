package com.auth.service.system.admin.excel.dept;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.delivery.SyncFileDelivery;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.service.system.admin.convert.spreadsheet.SysDeptSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.query.dept.SysDeptPageQuery;
import com.auth.service.system.admin.support.dept.DeptLookupSupport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 部门 Excel 导入导出
 *
 * @author Bunny
 */
@Service
public class SysDeptSpreadsheetService
		extends AbstractSyncPagedExportService<SysDeptPageQuery, SysDeptPageRowPO, SysDeptSpreadsheetRow> {

	private static final String SHEET_NAME = "部门";

	private static final String FILENAME_PREFIX = "部门";

	private static final int STATUS_COLUMN_INDEX = 6;

	private final SysDeptMapper sysDeptMapper;

	private final DeptLookupSupport deptLookupSupport;

	private final DeptSheetImporter deptSheetImporter;

	public SysDeptSpreadsheetService(SysDeptMapper sysDeptMapper, DeptLookupSupport deptLookupSupport,
			DeptSheetImporter deptSheetImporter) {
		super(ExcelExportWriter.withStatusStyle(SysDeptSpreadsheetRow.class, SHEET_NAME,
				new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX)), FILENAME_PREFIX);
		this.sysDeptMapper = sysDeptMapper;
		this.deptLookupSupport = deptLookupSupport;
		this.deptSheetImporter = deptSheetImporter;
	}

	/**
	 * 按筛选条件与页码区间导出部门 Excel
	 * @param query 列表筛选
	 * @param scope 导出范围
	 * @return 文件字节响应
	 * @throws IOException 写入失败
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysDeptPageQuery query, ExportScopeQueryFields scope) throws IOException {
		return super.export(query, scope);
	}

	@Override
	protected List<SysDeptPageRowPO> fetchPage(SysDeptPageQuery filter, int pageIndex, int pageSize) {
		Page<SysDeptEntity> pageParams = new Page<>(pageIndex, pageSize);
		IPage<SysDeptPageRowPO> page = sysDeptMapper.selectListByPage(pageParams, filter);
		return page.getRecords();
	}

	@Override
	protected List<SysDeptSpreadsheetRow> toExportRows(List<SysDeptPageRowPO> rows) {
		List<Long> deptIds = rows.stream().map(SysDeptPageRowPO::getId).toList();
		Map<Long, String> deptPathById = deptLookupSupport.resolvePaths(deptIds);

		return rows.stream().map(row -> {
			SysDeptSpreadsheetRow exportRow = SysDeptSpreadsheetConverter.INSTANCE.toSpreadsheetRow(row);
			exportRow.setDeptPath(deptPathById.getOrDefault(row.getId(), row.getDeptName()));
			return exportRow;
		}).toList();
	}

	/**
	 * 从上传文件批量导入部门（解析 → 校验 → 写入，全部通过才落库）
	 * @param file 上传的 Excel 文件
	 * @return 导入结果（含行级错误）
	 * @throws IOException 文件读取失败
	 */
	@Transactional(rollbackFor = Exception.class)
	public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
		return deptSheetImporter.doImport(file);
	}

	/**
	 * 生成部门导入模板并返回文件响应
	 * @return 模板文件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
		SysDeptImportRow row = new SysDeptImportRow();
		row.setParentDeptCode("（示例：ROOT，留空表示顶级部门）");
		row.setDeptCode("（示例：RD，全局唯一）");
		row.setDeptName("（示例：研发部）");
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("（可选）");
		byte[] bytes = new ExcelExportWriter<>(SysDeptImportRow.class, "部门导入模板").write(List.of(row));
		return SyncFileDelivery.deliver(bytes, "dept_import_template.xlsx");
	}

}
