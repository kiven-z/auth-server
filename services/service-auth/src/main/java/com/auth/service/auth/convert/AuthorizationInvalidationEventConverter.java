package com.auth.service.auth.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.auth.domain.authorization.invalidation.InvalidationProcessingMarker;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventDetailRowPO;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.domain.authorization.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.domain.authorization.model.vo.AuthorizationInvalidationEventPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * 授权失效幂等事件转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface AuthorizationInvalidationEventConverter {

	AuthorizationInvalidationEventConverter INSTANCE = Mappers.getMapper(AuthorizationInvalidationEventConverter.class);

	/**
	 * 分页行 PO → VO
	 * @param po 持久层投影
	 * @return 分页 VO
	 */
	@Mapping(target = "processing", source = "impactedUserCount", qualifiedByName = "toProcessingFlag")
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	AuthorizationInvalidationEventPageVO toPageVO(AuthorizationInvalidationEventPageRowPO po);

	/**
	 * 详情 PO → 详情 VO
	 * @param po 持久层详情投影
	 * @return 详情 VO
	 */
	@Mapping(target = "processing", source = "impactedUserCount", qualifiedByName = "toProcessingFlag")
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	AuthorizationInvalidationEventDetailVO toDetailVo(AuthorizationInvalidationEventDetailRowPO po);

	/**
	 * 影响面计数是否为处理中占位
	 * @param impactedUserCount 影响面用户数
	 * @return 处理中时为 true
	 */
	@Named("toProcessingFlag")
	default Boolean toProcessingFlag(Integer impactedUserCount) {
		return InvalidationProcessingMarker.isProcessing(impactedUserCount);
	}

}
