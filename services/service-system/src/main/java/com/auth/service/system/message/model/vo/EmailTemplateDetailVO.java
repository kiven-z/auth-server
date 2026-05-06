package com.auth.service.system.message.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import com.auth.service.system.message.model.EmailTemplateRequireFieldRow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 邮件模板详情返回对象（含 require_fields 解析结果）
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplateDetailVO", title = "邮件模板详情返回对象")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class EmailTemplateDetailVO extends BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "templateCode", title = "模板编码")
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称")
	private String templateName;

	@Schema(name = "description", title = "模板描述")
	private String description;

	@Schema(name = "subject", title = "邮件主题")
	private String subject;

	@Schema(name = "content", title = "邮件内容(支持HTML/FreeMarker)")
	private String content;

	@Schema(name = "requireFields", title = "变量声明列表")
	private List<EmailTemplateRequireFieldRow> requireFields;

	@Schema(name = "priority", title = "发送优先级")
	private Integer priority;

	@Schema(name = "status", title = "状态(0=禁用,1=启用)")
	private Integer status;

}
