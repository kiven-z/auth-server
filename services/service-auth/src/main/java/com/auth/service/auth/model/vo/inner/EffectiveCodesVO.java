package com.auth.service.auth.model.vo.inner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 内部接口返回：用户生效角色码与权限码
 *
 * @author Bunny
 */
@Schema(title = "生效角色与权限码")
@Data
@NoArgsConstructor
public class EffectiveCodesVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码列表")
	private List<String> roleCodes;

	@Schema(title = "权限编码列表")
	private List<String> permissionCodes;

}
