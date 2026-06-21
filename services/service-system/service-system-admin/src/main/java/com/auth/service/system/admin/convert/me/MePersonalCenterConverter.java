package com.auth.service.system.admin.convert.me;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import com.auth.service.system.admin.model.vo.me.MeLoginLogPageVO;
import com.auth.service.system.admin.model.vo.me.MeUserSessionVO;
import com.auth.service.system.admin.integration.auth.session.dto.UserSessionRemoteDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 个人中心 VO 映射
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface MePersonalCenterConverter {

	MePersonalCenterConverter INSTANCE = Mappers.getMapper(MePersonalCenterConverter.class);

	/**
	 * 内部会话 DTO → 个人中心会话 VO（current 由业务层填充）
	 * @param dto 内部会话 DTO
	 * @return 个人中心会话 VO
	 */
	@Mapping(target = "current", ignore = true)
	MeUserSessionVO toSessionVo(UserSessionRemoteDTO dto);

	/**
	 * 管理端登录日志分页行 → 个人中心精简 VO
	 * @param adminVo 管理端分页行
	 * @return 个人中心分页行
	 */
	MeLoginLogPageVO toLoginLogVo(LogLoginLogPageVO adminVo);

	/**
	 * 批量转换登录日志
	 * @param adminVos 管理端分页行列表
	 * @return 个人中心分页行列表
	 */
	List<MeLoginLogPageVO> toLoginLogVoList(List<LogLoginLogPageVO> adminVos);

}
