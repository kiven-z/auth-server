package com.auth.service.system.message.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.EmailTemplateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * 邮件模板转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface EmailTemplateConverter {

	EmailTemplateConverter INSTANCE = Mappers.getMapper(EmailTemplateConverter.class);

	/**
	 * 保存表单 → 新建实体（不含 id、channel、require_fields）
	 * @param form 邮件模板表单
	 * @return 待持久化实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "sceneCode", source = "templateCode")
	@Mapping(target = "bodyContent", source = "content")
	@Mapping(target = "channel", ignore = true)
	@Mapping(target = "requireFields", ignore = true)
	@Mapping(target = "providerTemplateCode", ignore = true)
	@Mapping(target = "imMessageType", ignore = true)
	@Mapping(target = "channelDefaultsJson", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "remark", ignore = true)
	MessageTemplateEntity toEntity(EmailTemplateForm form);

	/**
	 * 保存表单 → 覆盖已有实体（不修改 channel、require_fields）
	 * @param form 邮件模板表单
	 * @param entity 数据库已有实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "sceneCode", source = "templateCode")
	@Mapping(target = "bodyContent", source = "content")
	@Mapping(target = "channel", ignore = true)
	@Mapping(target = "requireFields", ignore = true)
	@Mapping(target = "providerTemplateCode", ignore = true)
	@Mapping(target = "imMessageType", ignore = true)
	@Mapping(target = "channelDefaultsJson", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "createUser", ignore = true)
	@Mapping(target = "updateUser", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "remark", ignore = true)
	void updateEntity(EmailTemplateForm form, @MappingTarget MessageTemplateEntity entity);

}
