package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryVO;
import com.auth.service.system.message.service.InAppMessageCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 站内信业务分类只读
 *
 * @author Bunny
 */
@AuthenticatedApi
@Tag(name = "站内信业务分类")
@Validated
@RequestMapping("/api/system/message/in-app/categories")
@RestController
public class InAppMessageCategoryController {

	private final InAppMessageCategoryService inAppMessageCategoryService;

	public InAppMessageCategoryController(InAppMessageCategoryService inAppMessageCategoryService) {
		this.inAppMessageCategoryService = inAppMessageCategoryService;
	}

	@Operation(summary = "大类列表", description = "启用中的大类（parent_id IS NULL）")
	@GetMapping("/majors")
	public Result<List<InAppMessageCategoryVO>> listMajors() {
		List<InAppMessageCategoryVO> data = inAppMessageCategoryService.listMajors();
		return Result.success(data);
	}

	@Operation(summary = "小类列表", description = "按大类 code 查询启用中的小类")
	@GetMapping("/children")
	public Result<List<InAppMessageCategoryVO>> listChildren(
			@RequestParam @NotBlank(message = "大类编码不能为空") String parentCode) {
		List<InAppMessageCategoryVO> data = inAppMessageCategoryService.listChildren(parentCode);
		return Result.success(data);
	}

}
