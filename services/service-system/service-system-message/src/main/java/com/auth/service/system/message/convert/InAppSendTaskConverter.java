package com.auth.service.system.message.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.po.InAppSendTaskPageRowPO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppSendTaskPageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 站内信发送任务转换器（读模型）
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface InAppSendTaskConverter {

	InAppSendTaskConverter INSTANCE = Mappers.getMapper(InAppSendTaskConverter.class);

	/**
	 * 分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	InAppSendTaskPageResponse toPageResponse(InAppSendTaskPageRowPO po);

	/**
	 * 实体 → 详情 VO（recipientScopeIds / includeChildren 由调用方按需回填）
	 * @param entity 发送任务实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	@Mapping(target = "recipientScopeIds", ignore = true)
	@Mapping(target = "includeChildren", ignore = true)
	InAppSendTaskDetailVO toDetailVo(InAppMessageEntity entity);

}
