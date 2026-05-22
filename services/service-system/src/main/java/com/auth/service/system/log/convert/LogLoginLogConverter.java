package com.auth.service.system.log.convert;

import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.system.log.model.po.LogLoginLogPageRowPO;
import com.auth.service.system.log.model.vo.LogLoginLogDetailVO;
import com.auth.service.system.log.model.vo.LogLoginLogPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 登录日志转换器
 *
 * @author Bunny
 */
@Mapper
public interface LogLoginLogConverter {

	LogLoginLogConverter INSTANCE = Mappers.getMapper(LogLoginLogConverter.class);

	/**
	 * 登录日志分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogLoginLogPageVO toPageVO(LogLoginLogPageRowPO po);

	/**
	 * 实体 → 详情 VO
	 * @param entity 登录日志实体
	 * @return 详情 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogLoginLogDetailVO toDetailVo(LoginLogEntity entity);

}
