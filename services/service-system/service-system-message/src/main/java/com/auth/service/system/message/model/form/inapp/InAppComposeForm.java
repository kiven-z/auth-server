package com.auth.service.system.message.model.form.inapp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理端站内信按范围发送表单（提交即为定稿正文，不再二次模板渲染）
 *
 * @author Bunny
 */
@Schema(name = "InAppComposeForm", title = "站内信按范围发送")
@Getter
@Setter
public class InAppComposeForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "接收范围类型", description = "USER / POST / DEPT / ALL")
	@NotBlank(message = "接收范围类型不能为空")
	private String recipientScopeType;

	@Schema(title = "范围 ID 列表", description = "USER/POST/DEPT 必填；ALL 可空")
	private List<Long> recipientScopeIds;

	@Schema(title = "部门是否包含子部门", description = "仅 DEPT 有效，默认 true")
	private Boolean includeChildren;

	@Schema(title = "模板场景编码", description = "可选；仅作起草来源溯源，发送时不渲染")
	private String templateCode;

	@Schema(title = "标题（定稿）")
	@NotBlank(message = "标题不能为空")
	private String title;

	@Schema(title = "正文（定稿）")
	@NotBlank(message = "正文不能为空")
	private String body;

	@Schema(title = "正文类型", description = "TEXT / MARKDOWN")
	@NotBlank(message = "正文类型不能为空")
	private String contentType;

	@Schema(title = "业务分类")
	private String bizType;

	@Schema(title = "跳转链接")
	private String linkUrl;

}
