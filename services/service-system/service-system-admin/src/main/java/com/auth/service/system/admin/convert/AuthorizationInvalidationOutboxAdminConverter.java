package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.po.authorizationinvalidation.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.admin.model.po.authorizationinvalidation.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 授权失效 Outbox 管理端转换器
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationInvalidationOutboxAdminConverter {

	AuthorizationInvalidationOutboxAdminConverter INSTANCE = Mappers
		.getMapper(AuthorizationInvalidationOutboxAdminConverter.class);

	/**
	 * 分页行 PO → VO
	 * @param po 持久层投影
	 * @return 分页 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	AuthorizationInvalidationOutboxPageVO toPageVO(AuthorizationInvalidationOutboxPageRowPO po);

	/**
	 * 详情 PO → 详情 VO
	 * @param po 持久层详情投影
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	AuthorizationInvalidationOutboxDetailVO toDetailVo(AuthorizationInvalidationOutboxDetailRowPO po);

}
