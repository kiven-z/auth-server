package com.auth.service.example.service;

import com.auth.service.example.model.vo.ExampleOrderVO;

import java.util.List;

/**
 * 数据权限演示单服务
 *
 * @author Bunny
 */
public interface ExampleOrderService {

	/**
	 * 查询当前用户可见的演示单列表
	 * @return 可见行
	 */
	List<ExampleOrderVO> listScoped();

}
