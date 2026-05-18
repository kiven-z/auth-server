package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 统一消息模板（sys_message_template），按 scene_code + channel 区分邮件、短信等。
 *
 * @author Bunny
 */
@TableName("sys_message_template")
@Schema(name = "MessageTemplateEntity", title = "统一消息模板")
@Getter
@Setter
@Accessors(chain = true)
public class MessageTemplateEntity extends BaseEntity {

	/**
	 * 业务场景编码（与发送命令中的 templateCode 一致：如 login-email-code、login-sms-code）
	 */
	@Schema(title = "场景编码")
	private String sceneCode;

	/**
	 * 渠道：EMAIL、SMS、DING_TALK 等，与 {@link com.auth.module.message.channel.MessageChannel} 名称一致
	 */
	@Schema(title = "渠道")
	private String channel;

	@Schema(title = "模板名称")
	private String templateName;

	@Schema(title = "模板描述")
	private String description;

	/**
	 * 邮件主题模板（FreeMarker）；短信等非邮件渠道可为空
	 */
	@Schema(title = "邮件主题")
	private String subject;

	/**
	 * 正文模板：邮件 HTML / 其它渠道富文本；短信国内一般不在库中拼全文
	 */
	@Schema(title = "正文模板")
	@TableField("body_content")
	private String bodyContent;

	/**
	 * 模板依赖变量（JSON 数组）
	 */
	@Schema(title = "必填变量声明 JSON")
	private String requireFields;

	/**
	 * 短信厂商侧已审模板编码（仅 SMS）
	 */
	@Schema(title = "厂商模板编码")
	private String providerTemplateCode;

	/**
	 * sms4j blend configId 等路由键（仅 SMS，可空走默认）
	 */
	@Schema(title = "短信配置 ID")
	private String smsConfigId;

	/**
	 * 钉钉等 IM 消息类型：TEXT、MARKDOWN 等
	 */
	@Schema(title = "IM 消息类型")
	private String imMessageType;

	/**
	 * 渠道默认扩展（JSON），与会话级 options 合并
	 */
	@Schema(title = "渠道默认选项 JSON")
	@TableField("channel_defaults_json")
	private String channelDefaultsJson;

	@Schema(title = "优先级（同 scene+channel 启用行取最大 priority）")
	private Integer priority;

	@Schema(title = "启用状态")
	private Boolean status;

}
