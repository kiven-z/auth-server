package com.auth.service.system.admin.model.vo.role;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 角色详情 VO
 *
 * @author Bunny
 */
@Schema(name = "SysRoleDetailVO", title = "角色详情")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleDetailVO extends BaseResponse {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "角色名称")
	private String roleName;

	@Schema(title = "启用状态(true=正常启用，false=停用)")
	private Boolean status;

	@Schema(title = "角色类型(1=系统,2=菜单,3=业务,4=自定义)")
	private Integer roleType;

	@Schema(title = "角色类型描述")
	private String roleTypeDesc;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "备注")
	private String remark;

}
