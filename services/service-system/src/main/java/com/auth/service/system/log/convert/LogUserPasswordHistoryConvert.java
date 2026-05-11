package com.auth.service.system.log.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.auth.service.system.log.model.po.LogUserPasswordHistoryPageRowPO;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryPageVO;

/**
 * 密码历史日志转换器
 *
 * @author Bunny
 */
@Mapper
public interface LogUserPasswordHistoryConvert {

	LogUserPasswordHistoryConvert INSTANCE = Mappers.getMapper(LogUserPasswordHistoryConvert.class);

	/**
	 * 密码历史日志分页行 PO → 分页返回 VO
	 * @param po 持久层查询投影
	 * @return 分页返回对象
	 */
	LogUserPasswordHistoryPageVO toPageVO(LogUserPasswordHistoryPageRowPO po);

}
