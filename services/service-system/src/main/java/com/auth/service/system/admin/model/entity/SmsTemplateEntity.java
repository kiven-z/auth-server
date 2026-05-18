package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 短信模板（业务 template_code 与厂商模板映射）
 *
 * @author Bunny
 */
@TableName("sys_sms_template")
@Schema(name = "SmsTemplateEntity", title = "短信模板")
@Getter
@Setter
@Accessors(chain = true)
public class SmsTemplateEntity extends BaseEntity {

	@Schema(title = "业务模板编码")
	private String templateCode;

	@Schema(title = "模板名称")
	private String templateName;

	@Schema(title = "描述")
	private String description;

	@Schema(title = "厂商：aliyun/tencent")
	private String provider;

	@Schema(title = "发送模式：VERIFY/NOTIFY")
	private String templateType;

	@Schema(title = "NOTIFY 时 sms4j configId")
	private String smsConfigId;

	@Schema(title = "厂商模板 CODE")
	private String providerTemplateCode;

	@Schema(title = "短信签名")
	private String signName;

	@Schema(title = "默认国家码")
	private String defaultCountryCode;

	@Schema(title = "必填变量 JSON")
	private String requireFields;

	@Schema(title = "优先级")
	private Integer priority;

	@Schema(title = "启用状态")
	private Boolean status;

}
