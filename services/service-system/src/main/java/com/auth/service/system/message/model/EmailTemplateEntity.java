package com.auth.service.system.message.model;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 邮件模板
 *
 * @author Bunny
 */
@TableName("sys_email_template")
@Schema(name = "EmailTemplateEntity", title = "邮件模板", description = "邮件模板的实体类对象")
@Getter
@Setter
@Accessors(chain = true)
public class EmailTemplateEntity extends BaseEntity {

	@Schema(name = "templateCode", title = "模板编码(唯一)")
	private String templateCode;

	@Schema(name = "templateName", title = "模板名称")
	private String templateName;

	@Schema(name = "description", title = "模板描述")
	private String description;

	@Schema(name = "subject", title = "邮件主题")
	private String subject;

	@Schema(name = "content", title = "邮件内容(支持HTML)")
	private String content;

	@Schema(name = "requireFields", title = "模板依赖变量(JSON字符串)")
	private String requireFields;

	@Schema(name = "priority", title = "发送优先级(1-10,数字越大优先级越高)")
	private Integer priority;

	@Schema(name = "status", title = "启用状态(true=启用模板，false=停用)")
	private Boolean status;

}
