package com.auth.module.file.export.datasource;

import com.auth.module.file.export.definition.ExportSheetMeta;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Map;

/**
 * 分页数据源（统一远程与本地取数抽象）
 *
 * @author Bunny
 */
public interface PageDataSource {

	/**
	 * 拉取分页数据
	 * @param pageNum 页码（1-based）
	 * @param pageSize 每页条数
	 * @return 分页数据
	 */
	IPage<Map<String, Object>> fetchPage(int pageNum, int pageSize);

	/**
	 * 工作表元信息
	 * @return 元信息
	 */
	ExportSheetMeta sheetMeta();

}
