package com.auth.service.system.admin.excel.user;

import com.auth.module.file.export.AbstractSyncPagedExportService;
import com.auth.module.file.export.ExportScopeQueryFields;
import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.convert.SysUserSpreadsheetConverter;
import com.auth.service.system.admin.excel.UserStatusColorCellWriteHandler;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.support.sqlbuild.SysUserPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	public SysUserSpreadsheetService(SysUserMapper sysUserMapper) {
		super(ExcelExportWriter.withStatusStyle(SysUserSpreadsheetRow.class, SHEET_NAME,
				new UserStatusColorCellWriteHandler(STATUS_COLUMN_INDEX)), FILENAME_PREFIX);
		this.sysUserMapper = sysUserMapper;
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

}
