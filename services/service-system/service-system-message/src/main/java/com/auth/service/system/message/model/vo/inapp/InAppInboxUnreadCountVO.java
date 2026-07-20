package com.auth.service.system.message.model.vo.inapp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 用户侧站内信未读角标
 *
 * @author Bunny
 */
@Schema(name = "InAppInboxUnreadCountVO", title = "站内信未读角标")
@Getter
@Builder
@ToString
public class InAppInboxUnreadCountVO {

	@Schema(title = "公开消息未读数（PULL）")
	private long pullUnreadCount;

	@Schema(title = "定向消息未读数（PUSH）")
	private long pushUnreadCount;

	@Schema(title = "未读总数")
	private long totalUnreadCount;

}
