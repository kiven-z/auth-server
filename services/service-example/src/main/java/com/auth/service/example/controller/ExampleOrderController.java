package com.auth.service.example.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.service.example.model.vo.ExampleOrderVO;
import com.auth.service.example.service.ExampleOrderService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据权限演示单接口
 *
 * @author Bunny
 */
@RequestMapping("/api/example/orders")
@RestController
public class ExampleOrderController {

	private final ExampleOrderService exampleOrderService;

	public ExampleOrderController(ExampleOrderService exampleOrderService) {
		this.exampleOrderService = exampleOrderService;
	}

	@AuthenticatedApi
	@Operation(summary = "查询数据权限演示单列表", description = "按登录画像 deptScope 行级过滤")
	@GetMapping
	public Result<List<ExampleOrderVO>> listScoped() {
		List<ExampleOrderVO> data = exampleOrderService.listScoped();
		return Result.success(data);
	}

}
