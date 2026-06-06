package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventPageVO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventInnerQuery;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 授权失效幂等事件管理端转换器
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationInvalidationEventAdminConverter {

	AuthorizationInvalidationEventAdminConverter INSTANCE = Mappers
		.getMapper(AuthorizationInvalidationEventAdminConverter.class);

	/**
	 * 管理端查询条件 → Feign 查询
	 * @param query 管理端查询
	 * @return Feign 查询
	 */
	AuthorizationInvalidationEventInnerQuery toInnerQuery(AuthorizationInvalidationEventQuery query);

	/**
	 * Feign 分页行 → 管理端分页 VO
	 * @param dto Feign 分页行
	 * @return 管理端分页 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	AuthorizationInvalidationEventPageVO toPageVO(AuthorizationInvalidationEventPageInnerDTO dto);

	/**
	 * Feign 详情 → 管理端详情 VO
	 * @param dto Feign 详情
	 * @return 管理端详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	AuthorizationInvalidationEventDetailVO toDetailVO(AuthorizationInvalidationEventDetailInnerDTO dto);

}
