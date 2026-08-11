package com.auth.service.system.admin.export.fetcher;

import com.auth.common.core.model.query.PageQueryRequest;
import com.auth.module.file.export.definition.ExportRowMaps;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

/**
 * 导出分页取数器模板：查询 → SpreadsheetRow → Map（fieldKey 对齐）
 *
 * @param <Q> 查询条件类型
 * @param <R> 持久层行类型（PO / Entity）
 * @param <S> Excel 表格行类型
 * @author Bunny
 */
public abstract class AbstractExportPageFetcher<Q extends PageQueryRequest, R, S> implements ExportPageFetcher<Q> {

	/**
	 * 查询分页原始行
	 * @param query 查询条件
	 * @param pageNum 页码（1-based）
	 * @param pageSize 每页条数
	 * @return 分页数据
	 */
	protected abstract IPage<R> queryPage(Q query, int pageNum, int pageSize);

	/**
	 * 原始行 → Excel 表格行
	 * @param row 持久层行
	 * @return 表格行
	 */
	protected abstract S toSpreadsheetRow(R row);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final IPage<Map<String, Object>> fetchPage(Q query, int pageNum, int pageSize) {
		IPage<R> page = queryPage(query, pageNum, pageSize);
		List<Map<String, Object>> records = page.getRecords()
			.stream()
			.map(this::toSpreadsheetRow)
			.map(ExportRowMaps::fromBean)
			.toList();
		Page<Map<String, Object>> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
		result.setRecords(records);
		return result;
	}

}
