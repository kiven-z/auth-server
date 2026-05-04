package com.auth.service.system.admin.role.model.vo;

import java.io.Serial;
import java.io.Serializable;

import com.auth.common.core.model.response.BaseResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页行
 *
 * @author Bunny
 */
@Schema(name = "SysRolePageResponse", title = "角色分页行")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePageResponse extends BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "状态(0=正常，1=禁用)")
	private Integer status;

	@Schema(title = "角色类型(1=系统,2=菜单,3=业务,4=自定义)")
	private Integer roleType;

	@Schema(title = "显示顺序")
	private Integer orderNum;

}
