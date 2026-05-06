package com.auth.service.system.common.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Feign 反序列化用：与 auth 服务 {@code EffectiveCodesVO} JSON 字段一致（roleCodes / permissionCodes）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class EffectiveCodesVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private List<String> roleCodes;

	private List<String> permissionCodes;

}
