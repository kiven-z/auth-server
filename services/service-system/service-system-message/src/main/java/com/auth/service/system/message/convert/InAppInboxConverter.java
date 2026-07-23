package com.auth.service.system.message.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.po.InAppInboxPageRowPO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppInboxPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户侧站内信收件箱转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface InAppInboxConverter {

	InAppInboxConverter INSTANCE = Mappers.getMapper(InAppInboxConverter.class);

	/**
	 * 分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	InAppInboxPageVO toPageVO(InAppInboxPageRowPO po);

	/**
	 * 主表实体 → 详情 VO（categoryName / isRead / readTime 由调用方回填）
	 * @param entity 站内信主表
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	@Mapping(target = "categoryName", ignore = true)
	@Mapping(target = "isRead", ignore = true)
	@Mapping(target = "readTime", ignore = true)
	InAppInboxDetailVO toDetailVO(InAppMessageEntity entity);

}
