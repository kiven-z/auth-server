package com.auth.service.system.email.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 邮件模板分页返回对象
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplatePageResponse", title = "邮件模板分页返回对象", description = "用于邮件模板分页查询返回数据")
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplatePageResponse extends BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "templateCode", title = "模板编码")
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称")
	private String templateName;

	@Schema(name = "subject", title = "邮件主题")
	private String subject;

	@Schema(name = "priority", title = "发送优先级")
	private Integer priority;

	@Schema(name = "status", title = "状态(0=禁用,1=启用)")
	private Integer status;

	@Schema(name = "description", title = "模板描述")
	private String description;

}
