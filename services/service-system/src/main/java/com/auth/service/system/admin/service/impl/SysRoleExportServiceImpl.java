package com.auth.service.system.admin.service.impl;

import com.auth.module.file.export.writer.ExcelExportWriter;
import com.auth.service.system.admin.convert.SysRoleExportConverter;
import com.auth.service.system.admin.excel.RoleTypeColorCellWriteHandler;
import com.auth.service.system.admin.excel.StatusColorCellWriteHandler;
import com.auth.service.system.admin.excel.export.AbstractSystemAdminExportService;
import com.auth.service.system.admin.excel.role.SysRoleExportRow;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.role.SysRoleExportQuery;
import com.auth.service.system.admin.service.SysRoleExportService;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * 角色 Excel 导出实现
 *
 * @author Bunny
 */
@Service
public class SysRoleExportServiceImpl
		extends AbstractSystemAdminExportService<SysRoleExportQuery, SysRoleEntity, SysRoleExportRow>
		implements SysRoleExportService {

	private static final String SHEET_NAME = "角色";

	private static final String FILENAME_PREFIX = "角色";

	private static final int ROLE_TYPE_COLUMN_INDEX = 2;

	private static final int STATUS_COLUMN_INDEX = 3;

	private final SysRoleMapper sysRoleMapper;

	public SysRoleExportServiceImpl(SysRoleMapper sysRoleMapper) {
		super(buildExcelWriter(), FILENAME_PREFIX);
		this.sysRoleMapper = sysRoleMapper;
	}

	private static ExcelExportWriter<SysRoleExportRow> buildExcelWriter() {
		return new ExcelExportWriter<>(SysRoleExportRow.class, SHEET_NAME)
			.registerWriteHandler(new RoleTypeColorCellWriteHandler(ROLE_TYPE_COLUMN_INDEX))
			.registerWriteHandler(new StatusColorCellWriteHandler(STATUS_COLUMN_INDEX));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> export(SysRoleExportQuery query) throws IOException {
		return super.export(query);
	}

	@Override
	protected List<SysRoleEntity> fetchPage(SysRoleExportQuery query, int pageIndex, int pageSize) {
		String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
		Page<SysRoleEntity> pageParams = new Page<>(pageIndex, pageSize);
		IPage<SysRoleEntity> page = sysRoleMapper.selectListByPage(pageParams, query, orderBySql);
		return page.getRecords();
	}

	@Override
	protected List<SysRoleExportRow> toExportRows(List<SysRoleEntity> rows) {
		return SysRoleExportConverter.INSTANCE.toExportRows(rows);
	}

}
