package com.auth.service.system.authorization.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.authorization.model.dto.AuthorizationInvalidationOutboxManualRetryOutcome;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxRetryResultVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Outbox 人工重试结果转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface AuthorizationInvalidationOutboxManualRetryConverter {

	AuthorizationInvalidationOutboxManualRetryConverter INSTANCE = Mappers
		.getMapper(AuthorizationInvalidationOutboxManualRetryConverter.class);

	/**
	 * 重试结果摘要 → 运维 VO
	 * @param outcome 重试结果摘要
	 * @return 运维 VO
	 */
	AuthorizationInvalidationOutboxRetryResultVO toRetryResultVo(
			AuthorizationInvalidationOutboxManualRetryOutcome outcome);

}
