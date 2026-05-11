package com.auth.service.system.common.feign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Feign 反序列化用：与 auth 服务 {@code EffectiveCodesVO} JSON 字段一致（roleCodes / permissionCodes）
 * <p>
 * 注意：命名后缀为 InnerDTO 表示是内部 Feign 传输对象，非 API 出参。
 * </p>
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class EffectiveCodesInnerDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色码")
	private List<String> roleCodes;

	@Schema(title = "权限码")
	private List<String> permissionCodes;

}
