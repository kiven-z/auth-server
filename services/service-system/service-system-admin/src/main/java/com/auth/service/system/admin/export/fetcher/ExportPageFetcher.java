package com.auth.service.system.admin.export.fetcher;

import com.auth.common.core.model.query.PageQueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Map;

/**
 * 导出分页取数器（业务回调实现）
 *
 * @param <Q> 查询条件类型
 * @author Bunny
 */
public interface ExportPageFetcher<Q extends PageQueryRequest> {

	/**
	 * 业务类型
	 * @return 业务类型
	 */
	String bizType();

	/**
	 * 查询条件类型
	 * @return 查询条件类型
	 */
	Class<Q> queryType();

	/**
	 * 拉取分页数据（Map 形式便于异构导出）
	 * @param query 查询条件
	 * @param pageNum 页码（1-based）
	 * @param pageSize 每页条数
	 * @return 分页数据
	 */
	IPage<Map<String, Object>> fetchPage(Q query, int pageNum, int pageSize);

}
