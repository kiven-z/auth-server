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
 * 站内信发送任务分页返回对象
 *
 * @author Bunny
 */
@Schema(name = "InAppSendTaskPageResponse", title = "站内信发送任务分页返回对象")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InAppSendTaskPageResponse extends BaseResponse {

	@Schema(name = "sourceType", title = "来源")
	private String sourceType;

	@Schema(name = "sceneCode", title = "场景编码")
	private String sceneCode;

	@Schema(name = "title", title = "标题")
	private String title;

	@Schema(name = "contentType", title = "正文类型")
	private String contentType;

	@Schema(name = "bizType", title = "业务分类")
	private String bizType;

	@JsonStringFormat
	@Schema(name = "senderUserId", title = "发起人用户 ID")
	private Long senderUserId;

	@Schema(name = "recipientScopeType", title = "接收范围类型")
	private String recipientScopeType;

	@Schema(name = "deliveryMode", title = "投递模式")
	private String deliveryMode;

	@Schema(name = "totalCount", title = "展开后目标总数")
	private Integer totalCount;

	@Schema(name = "successCount", title = "投递成功数")
	private Integer successCount;

	@Schema(name = "failCount", title = "投递失败数")
	private Integer failCount;

	@Schema(name = "status", title = "任务状态")
	private String status;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(name = "recalledAt", title = "撤回时间")
	private LocalDateTime recalledAt;

}
