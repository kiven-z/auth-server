package com.auth.service.auth.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventStatsVO;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventDetailRow;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventPageRow;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventStatsRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 授权失效幂等事件 Row → VO 映射
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class, uses = AuthorizationInvalidationEventMappingSupport.class)
public interface AuthorizationInvalidationEventConverter {

	AuthorizationInvalidationEventConverter INSTANCE = Mappers.getMapper(AuthorizationInvalidationEventConverter.class);

	/**
	 * 分页行投影 → 分页 VO
	 * @param row 分页行投影
	 * @return 分页 VO
	 */
	@Mapping(target = "processing", source = "impactedUserCount", qualifiedByName = "toProcessingFlag")
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	AuthorizationInvalidationEventPageVO toPageVO(AuthorizationInvalidationEventPageRow row);

	/**
	 * 详情行投影 → 详情 VO
	 * @param row 详情行投影
	 * @return 详情 VO
	 */
	@Mapping(target = "processing", source = "impactedUserCount", qualifiedByName = "toProcessingFlag")
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	AuthorizationInvalidationEventDetailVO toDetailVo(AuthorizationInvalidationEventDetailRow row);

	/**
	 * 统计行投影 → 统计 VO
	 * @param row 统计行投影
	 * @return 统计 VO
	 */
	AuthorizationInvalidationEventStatsVO toStatsVo(AuthorizationInvalidationEventStatsRow row);

}
