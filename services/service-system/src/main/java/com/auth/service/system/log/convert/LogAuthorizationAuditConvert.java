package com.auth.service.system.log.convert;

import com.auth.service.system.log.model.po.LogAuthorizationAuditPageRowPO;
import com.auth.service.system.log.model.vo.LogAuthorizationAuditPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 权限决策审计转换器
 *
 * @author Bunny
 */
@Mapper
public interface LogAuthorizationAuditConvert {

	LogAuthorizationAuditConvert INSTANCE = Mappers.getMapper(LogAuthorizationAuditConvert.class);

	/**
	 * 分页行 PO → VO
	 * @param po 持久层投影
	 * @return 分页 VO
	 */
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogAuthorizationAuditPageVO toPageVO(LogAuthorizationAuditPageRowPO po);

}
