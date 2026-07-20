package com.auth.service.system.message.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户侧站内信收件箱分页查询
 *
 * @author Bunny
 */
@Schema(name = "InAppInboxQuery", title = "站内信收件箱查询", description = "当前用户收件箱分页；按投递模式分 Tab")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InAppInboxQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "deliveryMode", title = "投递模式", description = "PULL=公开读扩散，PUSH=定向写扩散",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "投递模式不能为空")
	private String deliveryMode;

	@Schema(name = "isRead", title = "是否已读", description = "不传则不过滤；PULL 无收件箱行视为未读")
	private Boolean isRead;

	@Schema(name = "bizType", title = "业务分类")
	private String bizType;

	@Schema(name = "title", title = "标题关键字", description = "模糊匹配")
	private String title;

}
