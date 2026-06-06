package com.auth.service.system.admin.model.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色编码与名称展示行
 *
 * @author Bunny
 */
@Schema(name = "RoleCodeNameRow", title = "角色编码名称行")
@Getter
@Setter
public class RoleCodeNameRow implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

}
