package com.auth.service.system.authorization.ops.convert;

import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationOutboxOpsQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 授权失效 Outbox 运维转换器
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationInvalidationOutboxOpsConverter {

	AuthorizationInvalidationOutboxOpsConverter INSTANCE = Mappers
		.getMapper(AuthorizationInvalidationOutboxOpsConverter.class);

	/**
	 * API 查询条件 → Outbox 运维查询条件
	 * @param query API 分页查询
	 * @return 运维查询条件
	 */
	AuthorizationInvalidationOutboxOpsQuery toOpsQuery(AuthorizationInvalidationOutboxQuery query);

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
