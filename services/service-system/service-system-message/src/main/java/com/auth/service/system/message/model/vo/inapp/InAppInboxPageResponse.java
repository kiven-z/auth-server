package com.auth.service.system.message.model.vo.inapp;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户侧站内信收件箱分页返回对象
 *
 * @author Bunny
 */
@Schema(name = "InAppInboxPageResponse", title = "站内信收件箱分页返回对象")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InAppInboxPageResponse extends BaseResponse {

	@Schema(name = "sceneCode", title = "场景编码")
	private String sceneCode;

	@Schema(name = "title", title = "标题")
	private String title;

	@Schema(name = "contentType", title = "正文类型")
	private String contentType;

	@Schema(name = "bizType", title = "业务分类")
	private String bizType;

	@Schema(name = "linkUrl", title = "跳转链接")
	private String linkUrl;

	@JsonStringFormat
	@Schema(name = "senderUserId", title = "发起人用户 ID")
	private Long senderUserId;

	@Schema(name = "deliveryMode", title = "投递模式")
	private String deliveryMode;

	@Schema(name = "isRead", title = "是否已读")
	private Boolean isRead;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(name = "readTime", title = "已读时间")
	private LocalDateTime readTime;

}
