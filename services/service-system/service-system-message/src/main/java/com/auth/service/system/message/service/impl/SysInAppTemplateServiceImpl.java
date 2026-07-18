package com.auth.service.system.message.service.impl;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.MessageTemplateMapper;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.enums.InAppContentType;
import com.auth.service.system.message.model.form.inapp.InAppTemplateForm;
import com.auth.service.system.message.service.SysInAppTemplateService;
import com.auth.service.system.message.support.template.MessageTemplateRequireFieldsJsonSupport;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 站内信模板服务
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysInAppTemplateServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplateEntity>
		implements SysInAppTemplateService {

	/**
	 * 将表单字段写入实体
	 * @param form 站内信模板表单
	 * @param entity 目标实体
	 */
	private static void applyForm(InAppTemplateForm form, MessageTemplateEntity entity) {
		InAppContentType contentType = InAppContentType.from(form.getContentType());

		entity.setSceneCode(form.getTemplateCode());
		entity.setTemplateName(form.getTemplateName());
		entity.setSubject(form.getSubject());
		entity.setImMessageType(contentType.name());
		entity.setBodyContent(form.getContent());
		entity.setDescription(form.getDescription());
		entity.setStatus(form.getStatus());
		entity.setPriority(form.getPriority());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(InAppTemplateForm form) {
		MessageTemplateEntity entity = new MessageTemplateEntity();
		entity.setChannel(MessageChannel.IN_APP.name());
		applyForm(form, entity);
		entity.setRequireFields(MessageTemplateRequireFieldsJsonSupport.toJson(Collections.emptyList()));

		save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(InAppTemplateForm form) {
		MessageTemplateEntity exists = getById(form.getId());
		if (exists == null) {
			throw new MessageException(DATA_NOT_EXIST);
		}

		applyForm(form, exists);
		updateById(exists);
	}

}
