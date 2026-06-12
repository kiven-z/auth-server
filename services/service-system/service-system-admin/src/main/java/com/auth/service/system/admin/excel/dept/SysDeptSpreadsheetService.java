package com.auth.service.system.admin.excel.dept;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.delivery.SyncFileDelivery;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportLabelParser;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.service.system.admin.convert.SysDeptSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.query.SysDeptPageQuery;
import com.auth.service.system.admin.service.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptPathResolver;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	private final DeptPathResolver deptPathResolver;

	private final SysDeptService sysDeptService;

	public SysDeptSpreadsheetService(SysDeptMapper sysDeptMapper, DeptPathResolver deptPathResolver,
			SysDeptService sysDeptService) {
		super(ExcelExportWriter.withStatusStyle(SysDeptSpreadsheetRow.class, SHEET_NAME,
				new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX)), FILENAME_PREFIX);
		this.sysDeptMapper = sysDeptMapper;
		this.deptPathResolver = deptPathResolver;
		this.sysDeptService = sysDeptService;
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
		Map<Long, String> deptPathById = deptPathResolver.resolve(deptIds);

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
	public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
		return new DeptSheetImporter().doImport(file);
	}

	/**
	 * 生成部门导入模板并返回文件响应
	 *
	 * <p>
	 * 模板含一条示例行，方便用户了解填写格式。
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

	/**
	 * 部门导入器：封装行级校验与批量写入逻辑
	 */
	private final class DeptSheetImporter extends AbstractSheetImporter<SysDeptImportRow, SysDeptForm> {

		private Map<String, Long> parentIdByCode = Map.of();

		@Override
		protected Class<SysDeptImportRow> rowType() {
			return SysDeptImportRow.class;
		}

		@Override
		protected void beforeConvert(List<SysDeptImportRow> rows) {
			List<String> parentDeptCodes = rows.stream()
				.map(SysDeptImportRow::getParentDeptCode)
				.filter(CharSequenceUtil::isNotBlank)
				.map(String::trim)
				.distinct()
				.toList();
			if (parentDeptCodes.isEmpty()) {
				parentIdByCode = Map.of();
				return;
			}
			parentIdByCode = sysDeptMapper.selectActiveByDeptCodes(parentDeptCodes)
				.stream()
				.collect(Collectors.toMap(SysDeptEntity::getDeptCode, SysDeptEntity::getId, (left, right) -> left,
						HashMap::new));
		}

		@Override
		protected SysDeptForm convertRow(SysDeptImportRow row, int rowNum, List<ImportRowError> errors) {
			if (CharSequenceUtil.isBlank(row.getDeptCode())) {
				errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": deptCode is required"));
				return null;
			}
			if (CharSequenceUtil.isBlank(row.getDeptName())) {
				errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": deptName is required"));
				return null;
			}
			Long parentId = resolveParentId(row.getParentDeptCode(), rowNum, errors);
			if (parentId == null && CharSequenceUtil.isNotBlank(row.getParentDeptCode())) {
				return null;
			}
			SysDeptForm form = new SysDeptForm();
			form.setParentId(parentId != null ? parentId : 0L);
			form.setDeptCode(row.getDeptCode().trim());
			form.setDeptName(row.getDeptName().trim());
			form.setStatus(ImportLabelParser.parseStatus(row.getStatusLabel()));
			form.setOrderNum(row.getOrderNum());
			form.setRemark(row.getRemark());
			return form;
		}

		@Override
		protected void saveBatch(List<SysDeptForm> forms) {
			sysDeptService.importBatch(forms);
		}

		private Long resolveParentId(String parentDeptCode, int rowNum, List<ImportRowError> errors) {
			if (CharSequenceUtil.isBlank(parentDeptCode)) {
				return 0L;
			}
			String parentDeptCodeTrimmed = parentDeptCode.trim();
			Long parentId = parentIdByCode.get(parentDeptCodeTrimmed);
			if (parentId == null) {
				errors.add(new ImportRowError(rowNum,
						"Row " + rowNum + ": parentDeptCode '" + parentDeptCode + "' not found"));
				return null;
			}
			return parentId;
		}

	}

}
