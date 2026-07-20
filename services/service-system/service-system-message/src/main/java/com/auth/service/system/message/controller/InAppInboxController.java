package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.service.system.message.model.query.InAppInboxQuery;
import com.auth.service.system.message.model.vo.inapp.InAppInboxPageResponse;
import com.auth.service.system.message.model.vo.inapp.InAppInboxUnreadCountVO;
import com.auth.service.system.message.service.InAppInboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_SUCCESS;

/**
 * 用户侧站内信收件箱
 *
 * @author Bunny
 */
@AuthenticatedApi
@Tag(name = "站内信收件箱", description = "当前用户公开/定向消息分页、未读角标与已读；登录即可访问")
@Validated
@RequestMapping("/api/system/me/message/in-app")
@RestController
public class InAppInboxController {

	private final InAppInboxService inAppInboxService;

	public InAppInboxController(InAppInboxService inAppInboxService) {
		this.inAppInboxService = inAppInboxService;
	}

	@Operation(summary = "分页查询我的站内信")
	@GetMapping("/inbox")
	public Result<PageResponse<InAppInboxPageResponse>> getInboxPage(@Valid InAppInboxQuery query) {
		PageResponse<InAppInboxPageResponse> pageResponse = inAppInboxService.getInboxPage(query);
		return Result.success(pageResponse);
	}

	@Operation(summary = "查询我的站内信未读角标")
	@GetMapping("/inbox/unread-count")
	public Result<InAppInboxUnreadCountVO> getUnreadCount() {
		InAppInboxUnreadCountVO unreadCount = inAppInboxService.getUnreadCount();
		return Result.success(unreadCount);
	}

	@Operation(summary = "批量标已读")
	@PostMapping("/inbox/read")
	public Result<String> markRead(@RequestBody @NotEmpty(message = "消息ID列表不能为空") List<Long> messageIds) {
		inAppInboxService.markRead(messageIds);
		return Result.success(null, OPERATION_SUCCESS.getMessage());
	}

}
