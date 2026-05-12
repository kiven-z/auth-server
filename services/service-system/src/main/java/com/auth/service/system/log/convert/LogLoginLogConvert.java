package com.auth.service.system.log.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.auth.service.system.log.model.po.LogLoginLogPageRowPO;
import com.auth.service.system.log.model.vo.LogLoginLogPageVO;

/**
 * 登录日志转换器
 *
 * @author Bunny
 */
@Mapper
public interface LogLoginLogConvert {

	LogLoginLogConvert INSTANCE = Mappers.getMapper(LogLoginLogConvert.class);

	/**
	 * 登录日志分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogLoginLogPageVO toPageVO(LogLoginLogPageRowPO po);

}
