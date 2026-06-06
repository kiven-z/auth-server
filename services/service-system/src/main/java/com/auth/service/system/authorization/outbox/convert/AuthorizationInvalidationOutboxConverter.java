package com.auth.service.system.authorization.outbox.convert;

import com.auth.service.system.authorization.outbox.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.outbox.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.outbox.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.outbox.model.vo.AuthorizationInvalidationOutboxPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 授权失效 Outbox 转换器
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationInvalidationOutboxConverter {

	AuthorizationInvalidationOutboxConverter INSTANCE = Mappers
		.getMapper(AuthorizationInvalidationOutboxConverter.class);

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
