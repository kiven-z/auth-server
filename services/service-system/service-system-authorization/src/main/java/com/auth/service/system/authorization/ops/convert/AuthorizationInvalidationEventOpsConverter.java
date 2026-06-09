package com.auth.service.system.authorization.ops.convert;

import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationEventPageVO;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationEventOpsQuery;
import com.auth.service.system.authorization.integration.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.authorization.integration.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 授权失效幂等事件运维转换器
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationInvalidationEventOpsConverter {

	AuthorizationInvalidationEventOpsConverter INSTANCE = Mappers
		.getMapper(AuthorizationInvalidationEventOpsConverter.class);

	/**
	 * API 查询条件 → 运维查询
	 * @param query API 查询
	 * @return 运维查询
	 */
	AuthorizationInvalidationEventOpsQuery toOpsQuery(AuthorizationInvalidationEventQuery query);

	/**
	 * Feign 分页行 → 分页 VO
	 * @param dto Feign 分页行
	 * @return 分页 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	AuthorizationInvalidationEventPageVO toPageVO(AuthorizationInvalidationEventPageInnerDTO dto);

	/**
	 * Feign 详情 → 详情 VO
	 * @param dto Feign 详情
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	AuthorizationInvalidationEventDetailVO toDetailVO(AuthorizationInvalidationEventDetailInnerDTO dto);

}
