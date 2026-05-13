package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.po.emailtemplate.EmailTemplatePageRowPO;
import com.auth.service.system.admin.model.vo.emailtemplate.EmailTemplatePageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 邮件模板转换器
 *
 * @author Bunny
 */
@Mapper
public interface EmailTemplateConvert {

	EmailTemplateConvert INSTANCE = Mappers.getMapper(EmailTemplateConvert.class);

	/**
	 * 邮件模板分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	EmailTemplatePageResponse toPageResponse(EmailTemplatePageRowPO po);

}
