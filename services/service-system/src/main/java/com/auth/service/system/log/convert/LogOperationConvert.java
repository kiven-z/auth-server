package com.auth.service.system.log.convert;

import com.auth.service.system.log.model.po.LogOperationPageRowPO;
import com.auth.service.system.log.model.vo.LogOperationPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 操作日志 PO / VO 转换。
 *
 * @author Bunny
 */
@Mapper
public interface LogOperationConvert {

	LogOperationConvert INSTANCE = Mappers.getMapper(LogOperationConvert.class);

	/**
	 * 分页行 PO → VO（操作用户名、审计展示名由 Service 填充；列表 IP 由 {@code @Desensitized} 在 JSON 输出时脱敏）。
	 *
	 * @param po 查询投影
	 * @return 分页 VO
	 */
	@Mapping(target = "username", ignore = true)
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogOperationPageVO toPageVO(LogOperationPageRowPO po);

}
