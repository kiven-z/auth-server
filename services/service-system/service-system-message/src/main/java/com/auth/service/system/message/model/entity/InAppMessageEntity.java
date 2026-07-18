package com.auth.service.system.message.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 站内信发送任务（一次发送动作）
 *
 * @author Bunny
 */
@TableName("in_app_message")
@Schema(name = "InAppMessageEntity", title = "站内信发送任务")
@Getter
@Setter
@Accessors(chain = true)
public class InAppMessageEntity extends BaseEntity {

	@Schema(title = "来源")
	private String sourceType;

	@Schema(title = "场景编码")
	private String sceneCode;

	@Schema(title = "标题")
	private String title;

	@Schema(title = "正文类型")
	private String contentType;

	@Schema(title = "正文")
	private String content;

	@Schema(title = "业务分类")
	private String bizType;

	@Schema(title = "跳转链接")
	private String linkUrl;

	@Schema(title = "发起人用户 ID")
	private Long senderUserId;

	@Schema(title = "接收范围类型")
	private String recipientScopeType;

	@Schema(title = "接收范围快照 JSON")
	private String recipientScopeJson;

	@Schema(title = "投递模式", description = "PULL=公开读扩散，PUSH=定向写扩散；由范围类型推导")
	private String deliveryMode;

	@Schema(title = "展开后目标总数")
	private Integer totalCount;

	@Schema(title = "投递成功数")
	private Integer successCount;

	@Schema(title = "投递失败数")
	private Integer failCount;

	@Schema(title = "任务状态")
	private String status;

	@Schema(title = "撤回时间")
	private LocalDateTime recalledAt;

	@Schema(title = "撤回操作人")
	private Long recallUserId;

}
