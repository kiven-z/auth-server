package com.auth.service.system.admin.convert;

import com.auth.service.system.admin.model.entity.LogOperationEntity;
import com.auth.service.system.admin.model.po.logoperation.LogOperationPageRowPO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationDetailVO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationPageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 操作日志 PO / VO 转换
 *
 * @author Bunny
 */
@Mapper
public interface LogOperationConverter {

	LogOperationConverter INSTANCE = Mappers.getMapper(LogOperationConverter.class);

	/**
	 * 分页行 PO → VO（操作用户名、审计展示名由 Service 填充；列表 IP 由 {@code @Desensitized} 在 JSON 输出时脱敏）
	 * @param po 查询投影
	 * @return 分页 VO
	 */
	@Mapping(target = "username", ignore = true)
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogOperationPageVO toPageVO(LogOperationPageRowPO po);

	/**
	 * 实体 → 详情 VO（操作用户展示名由 Service enrich）
	 * @param entity 操作日志实体
	 * @return 详情 VO
	 */
	@Mapping(target = "username", ignore = true)
	@Mapping(target = "createUsername", ignore = true)
	@Mapping(target = "updateUsername", ignore = true)
	LogOperationDetailVO toDetailVo(LogOperationEntity entity);

}
