package com.auth.service.system.message.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.message.model.entity.MessageTemplateEntity;
import com.auth.service.system.message.model.form.EmailTemplateForm;
import com.auth.service.system.message.model.po.EmailTemplatePageRowPO;
import com.auth.service.system.message.model.vo.EmailTemplateDetailVO;
import com.auth.service.system.message.model.vo.EmailTemplatePageResponse;
import com.auth.service.system.message.model.vo.EmailTemplateRequireFieldRow;
import com.auth.service.system.message.template.MessageTemplateRequireFieldsJsonSupport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 邮件模板转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface EmailTemplateConverter {

	EmailTemplateConverter INSTANCE = Mappers.getMapper(EmailTemplateConverter.class);

	/**
	 * 数据库 require_fields JSON → 变量声明列表
	 * @param json JSON 字符串
	 * @return 变量声明列表
	 */
	@Named("parseRequireFields")
	default List<EmailTemplateRequireFieldRow> parseRequireFields(String json) {
		return MessageTemplateRequireFieldsJsonSupport.parse(json);
	}

	/**
	 * 邮件模板分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	EmailTemplatePageResponse toPageResponse(EmailTemplatePageRowPO po);

	/**
	 * 实体 → 详情 VO（scene_code / body_content 与 API 字段对齐）
	 * @param entity 邮件模板实体
	 * @return 详情 VO
	 */
	@Mapping(target = "templateCode", source = "sceneCode")
	@Mapping(target = "content", source = "bodyContent")
	@Mapping(target = "requireFields", source = "requireFields", qualifiedByName = "parseRequireFields")
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	EmailTemplateDetailVO toDetailVo(MessageTemplateEntity entity);

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
