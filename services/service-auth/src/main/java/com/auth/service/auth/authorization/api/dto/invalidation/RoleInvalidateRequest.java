package com.auth.service.auth.authorization.api.dto.invalidation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按角色码触发授权失效的请求体。
 *
 * @author Bunny
 */
@Schema(name = "RoleInvalidateRequest", title = "角色授权失效请求")
@Getter
@Setter
public class RoleInvalidateRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@NotEmpty(message = "roleCodes must not be empty")
	@Schema(title = "角色编码列表")
	private List<String> roleCodes;

	@NotBlank(message = "eventId must not be blank")
	@Schema(title = "业务事件 ID（幂等键，后续补偿任务使用）")
	private String eventId;

}
