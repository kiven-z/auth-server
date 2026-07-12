package com.auth.service.system.message.service;

import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.sms.SmsTemplateForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 短信模板服务
 *
 * @author Bunny
 */
public interface SysSmsTemplateService extends IService<MessageTemplateEntity> {

	/**
	 * 添加短信模板
	 * @param form 短信模板表单
	 */
	void create(SmsTemplateForm form);

}
