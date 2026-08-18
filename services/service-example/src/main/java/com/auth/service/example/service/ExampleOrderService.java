package com.auth.service.example.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.example.model.entity.ExampleOrderEntity;
import com.auth.service.example.model.query.ExampleOrderQuery;
import com.auth.service.example.model.vo.ExampleOrderVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 数据权限演示单服务
 *
 * @author Bunny
 */
public interface ExampleOrderService {

	/**
	 * 分页查询当前用户可见的演示单
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<ExampleOrderVO> getPage(ExampleOrderQuery query);

	/**
	 * 分页查询当前用户可见的演示单（供异步导出取数）
	 * @param query 查询条件
	 * @param pageNum 页码（1-based）
	 * @param pageSize 每页条数
	 * @return 分页实体
	 */
	IPage<ExampleOrderEntity> exportPage(ExampleOrderQuery query, int pageNum, int pageSize);

}
