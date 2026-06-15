package com.auth.service.auth.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.api.dto.authorization.EffectiveCodesVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 生效角色/权限码：AuthProfile → 内部 API VO
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface EffectiveCodesConverter {

	EffectiveCodesConverter INSTANCE = Mappers.getMapper(EffectiveCodesConverter.class);

	/**
	 * 授权画像 → 生效码 VO
	 * @param profile 授权画像
	 * @return VO
	 */
	@Mapping(target = "roleCodes", source = "roles")
	@Mapping(target = "permissionCodes", source = "permissions")
	EffectiveCodesVO toVo(AuthProfile profile);

	/**
	 * 画像为空时返回空列表
	 * @param profile 授权画像，可为 null
	 * @return VO
	 */
	default EffectiveCodesVO toVoOrEmpty(AuthProfile profile) {
		if (profile == null) {
			EffectiveCodesVO vo = new EffectiveCodesVO();
			vo.setRoleCodes(List.of());
			vo.setPermissionCodes(List.of());
			return vo;
		}
		return toVo(profile);
	}

}
