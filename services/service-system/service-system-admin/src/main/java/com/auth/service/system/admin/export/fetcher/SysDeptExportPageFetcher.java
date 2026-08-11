package com.auth.service.system.admin.export.fetcher;

import com.auth.module.security.contract.api.audit.PlatformBizCodes;
import com.auth.service.system.admin.convert.spreadsheet.SysDeptSpreadsheetConverter;
import com.auth.service.system.admin.excel.dept.SysDeptSpreadsheetRow;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.query.dept.SysDeptPageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 部门导出分页取数
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class SysDeptExportPageFetcher
		extends AbstractExportPageFetcher<SysDeptPageQuery, SysDeptPageRowPO, SysDeptSpreadsheetRow> {

	private final SysDeptMapper sysDeptMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String bizType() {
		return PlatformBizCodes.SYS_DEPT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<SysDeptPageQuery> queryType() {
		return SysDeptPageQuery.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IPage<SysDeptPageRowPO> queryPage(SysDeptPageQuery query, int pageNum, int pageSize) {
		Page<SysDeptEntity> pageParams = new Page<>(pageNum, pageSize);
		return sysDeptMapper.selectListByPage(pageParams, query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SysDeptSpreadsheetRow toSpreadsheetRow(SysDeptPageRowPO row) {
		return SysDeptSpreadsheetConverter.INSTANCE.toSpreadsheetRow(row);
	}

}
