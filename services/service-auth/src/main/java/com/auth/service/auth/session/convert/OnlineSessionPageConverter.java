package com.auth.service.auth.session.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.domain.model.UserOnlineBrief;
import com.auth.service.auth.session.domain.model.vo.OnlineSessionPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 全局在线会话分页行转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface OnlineSessionPageConverter {

	OnlineSessionPageConverter INSTANCE = Mappers.getMapper(OnlineSessionPageConverter.class);

	/**
	 * 会话索引与用户快照合并为分页行
	 * @param session 会话索引
	 * @param userBrief 用户展示快照，可为 null
	 * @return 分页行
	 */
	@Mapping(target = "username", source = "userBrief.username")
	@Mapping(target = "nickname", source = "userBrief.nickname")
	OnlineSessionPageVO toPageVo(UserSessionIndex session, UserOnlineBrief userBrief);

}
