package com.auth.service.system.admin.excel.dept;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.model.enums.ZebraStripeStyleType;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.service.system.admin.convert.SysDeptSpreadsheetConverter;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.query.SysDeptQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * 部门 Excel 导出
 *
 * @author Bunny
 */
@Service
public class SysDeptSpreadsheetService
		extends AbstractSyncPagedExportService<SysDeptQuery, SysDeptPageRowPO, SysDeptSpreadsheetRow> {

	private static final String SHEET_NAME = "部门";

	private static final String FILENAME_PREFIX = "部门";

	private static final int STATUS_COLUMN_INDEX = 5;

	private final SysDeptMapper sysDeptMapper;

	public SysDeptSpreadsheetService(SysDeptMapper sysDeptMapper) {
		super(buildExcelWriter(), FILENAME_PREFIX);
		this.sysDeptMapper = sysDeptMapper;
	}

	/**
	 * 构建 Excel 导出 writer
	 * @return Excel 导出 writer
	 */
	private static ExcelExportWriter<SysDeptSpreadsheetRow> buildExcelWriter() {
		return new ExcelExportWriter<>(SysDeptSpreadsheetRow.class, SHEET_NAME)
			.registerWriteHandler(ZebraStripeStyleType.getHandler(ZebraStripeStyleType.BLUE_GREY_TECH))
			.registerWriteHandler(new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX));
	}

	/**
	 * 按筛选条件与页码区间导出部门 Excel
	 * @param filter 列表筛选
	 * @param scope 导出范围
	 * @return 文件字节响应
	 * @throws IOException 写入失败
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysDeptQuery filter, ExportScopeQueryFields scope) throws IOException {
		return super.export(filter, scope);
	}

	@Override
	protected List<SysDeptPageRowPO> fetchPage(SysDeptQuery filter, int pageIndex, int pageSize) {
		Page<SysDeptEntity> pageParams = new Page<>(pageIndex, pageSize);
		IPage<SysDeptPageRowPO> page = sysDeptMapper.selectListByPage(pageParams, filter);
		return page.getRecords();
	}

	@Override
	protected List<SysDeptSpreadsheetRow> toExportRows(List<SysDeptPageRowPO> rows) {
		return SysDeptSpreadsheetConverter.INSTANCE.toSpreadsheetRows(rows);
	}

}
