package com.auth.service.system.message.controller;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.message.model.query.MessageChannelDeliveryQuery;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryDetailVO;
import com.auth.service.system.message.model.vo.delivery.MessageChannelDeliveryPageResponse;
import com.auth.service.system.message.service.MessageChannelDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 渠道投递记录接口
 *
 * @author Bunny
 */
@Tag(name = "渠道投递记录", description = "EMAIL/SMS/DING_TALK 等渠道投递结果查询")
@Validated
@RequestMapping("/api/system/message/channel-delivery")
@RestController
public class MessageChannelDeliveryController {

	private final MessageChannelDeliveryService messageChannelDeliveryService;

	public MessageChannelDeliveryController(MessageChannelDeliveryService messageChannelDeliveryService) {
		this.messageChannelDeliveryService = messageChannelDeliveryService;
	}

	@Operation(summary = "分页查询渠道投递记录", description = "可按 taskId、channel、status 过滤")
	@PreAuthorize("@auth.decide('message:delivery:query')")
	@GetMapping()
	public Result<PageResponse<MessageChannelDeliveryPageResponse>> getChannelDeliveryPage(
			@Valid MessageChannelDeliveryQuery query) {
		PageResponse<MessageChannelDeliveryPageResponse> pageResponse = messageChannelDeliveryService
			.getChannelDeliveryPage(query);
		return Result.success(pageResponse);
	}

	@Operation(summary = "渠道投递记录详情", description = "按主键返回完整投递字段")
	@PreAuthorize("@auth.decide('message:delivery:detail')")
	@GetMapping("{id}")
	public Result<MessageChannelDeliveryDetailVO> getChannelDeliveryById(@PathVariable("id") Long id) {
		MessageChannelDeliveryDetailVO detailVO = messageChannelDeliveryService.getChannelDeliveryById(id);
		return Result.success(detailVO);
	}

}
