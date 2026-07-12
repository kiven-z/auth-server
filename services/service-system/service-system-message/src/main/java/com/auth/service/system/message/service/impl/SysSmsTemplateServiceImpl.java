package com.auth.service.system.message.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.sms.SmsTemplateForm;
import com.auth.service.system.message.service.SysSmsTemplateService;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 短信模板服务。
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysSmsTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplateEntity>
		implements SysSmsTemplateService {

	public SysSmsTemplateServiceImpl(MessageTemplateMapper messageTemplateMapper) {
		this.baseMapper = messageTemplateMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(SmsTemplateForm form) {
		MessageTemplateEntity entity = new MessageTemplateEntity();
		entity.setChannel(MessageChannel.SMS.name());
		entity.setSceneCode(form.getTemplateCode());
		entity.setTemplateName(form.getTemplateName());
		entity.setProviderTemplateCode(form.getProviderTemplateCode());
		entity.setDescription(form.getDescription());
		entity.setStatus(form.getStatus());
		entity.setPriority(form.getPriority());
		entity.setBodyContent(CharSequenceUtil.nullToDefault(form.getContent(), ""));
		entity.setRequireFields(MessageTemplateRequireFieldsJsonSupport.toJson(Collections.emptyList()));

		save(entity);
	}

}
