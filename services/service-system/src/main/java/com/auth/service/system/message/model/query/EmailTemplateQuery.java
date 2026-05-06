package com.auth.service.system.message.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 邮件模板查询
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplateQuery", title = "邮件模板查询", description = "邮件模板分页查询参数")
@Data
@EqualsAndHashCode(callSuper = true)
public class EmailTemplateQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(name = "templateCode", title = "模板编码")
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称")
	private String templateName;

	@Schema(name = "subject", title = "邮件主题")
	private String subject;

	@Schema(name = "status", title = "状态(0=禁用,1=启用)")
	private Integer status;

}
