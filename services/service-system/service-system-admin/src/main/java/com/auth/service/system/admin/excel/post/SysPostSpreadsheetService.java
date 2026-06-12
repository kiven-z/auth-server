package com.auth.service.system.admin.excel.post;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.delivery.SyncFileDelivery;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportLabelParser;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.service.system.admin.convert.SysPostSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.service.SysPostService;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

	private final SysDeptMapper sysDeptMapper;

	private final SysPostService sysPostService;

	public SysPostSpreadsheetService(SysPostMapper sysPostMapper, SysDeptMapper sysDeptMapper,
			SysPostService sysPostService) {
		super(ExcelExportWriter.withStatusStyle(SysPostSpreadsheetRow.class, SHEET_NAME,
				new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX)), FILENAME_PREFIX);
		this.sysPostMapper = sysPostMapper;
		this.sysDeptMapper = sysDeptMapper;
		this.sysPostService = sysPostService;
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

	/**
	 * 从上传文件批量导入岗位（解析 → 校验 → 写入，全部通过才落库）
	 * @param file 上传的 Excel 文件
	 * @return 导入结果（含行级错误）
	 * @throws IOException 文件读取失败
	 */
	public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
		return new PostSheetImporter().doImport(file);
	}

	/**
	 * 生成岗位导入模板并返回文件响应
	 * @return 模板文件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
		SysPostImportRow row = new SysPostImportRow();
		row.setDeptCode("（示例：RD，部门必须已存在）");
		row.setPostCode("（示例：P01，同部门内唯一）");
		row.setPostName("（示例：高级工程师）");
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("（可选）");
		byte[] bytes = new ExcelExportWriter<>(SysPostImportRow.class, "岗位导入模板").write(List.of(row));
		return SyncFileDelivery.deliver(bytes, "post_import_template.xlsx");
	}

	/**
	 * 岗位导入器：封装行级校验与批量写入逻辑
	 */
	private final class PostSheetImporter extends AbstractSheetImporter<SysPostImportRow, SysPostForm> {

		@Override
		protected Class<SysPostImportRow> rowType() {
			return SysPostImportRow.class;
		}

		@Override
		protected SysPostForm convertRow(SysPostImportRow row, int rowNum, List<ImportRowError> errors) {
			if (CharSequenceUtil.isBlank(row.getDeptCode())) {
				errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": deptCode is required"));
				return null;
			}
			if (CharSequenceUtil.isBlank(row.getPostCode())) {
				errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": postCode is required"));
				return null;
			}
			if (CharSequenceUtil.isBlank(row.getPostName())) {
				errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": postName is required"));
				return null;
			}
			Long deptId = resolveDeptId(row.getDeptCode(), rowNum, errors);
			if (deptId == null) {
				return null;
			}
			SysPostForm form = new SysPostForm();
			form.setDeptId(deptId);
			form.setPostCode(row.getPostCode().trim());
			form.setPostName(row.getPostName().trim());
			form.setStatus(ImportLabelParser.parseStatus(row.getStatusLabel()));
			form.setOrderNum(row.getOrderNum());
			form.setRemark(row.getRemark());
			return form;
		}

		@Override
		protected void saveBatch(List<SysPostForm> forms) {
			sysPostService.importBatch(forms);
		}

		private Long resolveDeptId(String deptCode, int rowNum, List<ImportRowError> errors) {
			SysDeptEntity dept = sysDeptMapper
				.selectOne(new LambdaQueryWrapper<SysDeptEntity>().eq(SysDeptEntity::getDeptCode, deptCode.trim()));
			if (dept == null) {
				errors.add(new ImportRowError(rowNum, "Row " + rowNum + ": deptCode '" + deptCode + "' not found"));
				return null;
			}
			return dept.getId();
		}

	}

}
