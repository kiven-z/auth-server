package com.auth.service.system.message.model.po;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 邮件模板分页行（持久层查询投影，非 API 出参）
 *
 * @author Bunny
 */
@Schema(name = "EmailTemplatePageRowPO", title = "邮件模板分页行（PO）")
@Getter
@Setter
@ToString
public class EmailTemplatePageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "模板编码")
	private String templateCode;

	@Schema(title = "模板名称")
	private String templateName;

	@Schema(title = "邮件主题")
	private String subject;

	@Schema(title = "发送优先级")
	private Integer priority;

	@Schema(title = "启用状态(true=启用模板，false=停用)")
	private Boolean status;

	@Schema(title = "模板描述")
	private String description;

	@Schema(title = "创建时间")
	private LocalDateTime createTime;

	@Schema(title = "更新时间")
	private LocalDateTime updateTime;

	@Schema(title = "创建用户")
	private Long createUser;

	@Schema(title = "更新用户")
	private Long updateUser;

}
